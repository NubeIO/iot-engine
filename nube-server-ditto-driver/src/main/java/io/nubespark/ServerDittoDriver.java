package io.nubespark;

import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.*;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.types.MessageSource;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by topsykretts on 5/11/18.
 */
public class ServerDittoDriver extends MicroServiceVerticle {

    private static final String EDGE_DITTO_DRIVER = "io.nubespark.edge.ditto.driver";
    private static final String SERVER_DITTO_DRIVER = "io.nubespark.server.ditto.driver";
    private static final String DITTO_EVENTS = "io.nubespark.ditto.events";
    private Logger logger = LoggerFactory.getLogger(ServerDittoDriver.class);

    private Boolean first = true;
    private Boolean checkPong = false;
    private long lastPongTs = 0;
    private WebSocket dittoWebSocket;

    @Override
    public void start() {
        super.start();

        HttpClient client = vertx.createHttpClient(new HttpClientOptions()
                .setVerifyHost(false)
                .setTrustAll(true)
                .setTcpKeepAlive(true)
        );


        //todo find way to keep connection alive and reconnect on network failure
        handleDittoWebSocket(client);

        handleWebServer(client);

        publishMessageSource(SERVER_DITTO_DRIVER, SERVER_DITTO_DRIVER, ar-> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Published Ditto Server message source");
            }
        });

        publishMessageSource(DITTO_EVENTS, DITTO_EVENTS, ar-> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Published Ditto Events message source");
            }
        });

        //This is message received from Edge ditto driver
        MessageSource.<JsonObject>getConsumer(discovery, new JsonObject().put("name", SERVER_DITTO_DRIVER), ar -> {
            if (ar.failed()) {
                logger.error("Message source {} is not discovered.",SERVER_DITTO_DRIVER);
            } else {
                MessageConsumer<JsonObject> consumer = ar.result();
                consumer.handler(message -> {
                    System.out.println("Received message:: " + Json.encodePrettily(message.body()));
                    // Request ditto server and send response ...
                    requestDittoServer(client, message.body(), dittoResHandler -> {
                        JsonObject dittoResponse = dittoResHandler.result();
                        message.reply(dittoResponse);
                    });
                });
            }
        });

        //re-establishing websocket with ditto
        vertx.setPeriodic(1000, handler-> {
            if(checkPong && lastPongTs != 0) {
                long now = new Date().getTime();
                long diff = now - lastPongTs;
                if(diff/(1000) > 2) {
                    System.out.println("Pong not received for = " + diff);
                    //todo re-establish connection if automatically not established by vertx.
                }
            }
        });
    }


    private void handleWebServer(HttpClient client) {
        vertx.createHttpServer().requestHandler(req -> {
            JsonObject request = new JsonObject();
            request.put("method", req.method().toString());
            request.put("uri", req.uri());

            if(req.method().equals(HttpMethod.GET)) {
                System.out.println("Proxying request: " + req.uri());
                requestDittoServer(client, request, dittoResHandler -> {
                    JsonObject dittoRes = dittoResHandler.result();
                    proxyDittoResponse(dittoRes, req);
                });
            } else {
                req.bodyHandler(body -> {
                    if (body!= null) {
                        request.put("body", body.getBytes());
                    }
                    System.out.println(Json.encodePrettily(request));
                    System.out.println("Forwarding request to check with edge driver");
                    vertx.eventBus().send(EDGE_DITTO_DRIVER, request, messageHandler -> {
                        if (messageHandler.succeeded()) {
                            JsonObject message = (JsonObject) messageHandler.result().body();
                            //Check if request is acknowledged by edge device
                            if(message.getInteger("statusCode") == 200) {
                                System.out.println("Received acknowledgement from edge");
                                //Execute actual request...
                                requestDittoServer(client, request, dittoResHandler -> {
                                    JsonObject dittoRes = dittoResHandler.result();
                                    System.out.println("Got response from ditto");
                                    proxyDittoResponse(dittoRes, req);
                                });
                            } else {
                                System.out.println("Received error from edge");
                                // Return error from edge to client
                                req.response()
                                        .setStatusCode(409)
                                        .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                                        .end(Json.encodePrettily(new JsonObject()
                                                .put("message", "Edge device gave status code " + message.getInteger("statusCode"))
                                                .put("body", Buffer.buffer(message.getBinary("body")).toString("ISO-8859-1"))
                                        ));
                            }
                        } else {
                            System.out.println("Problem in receiving message from edge");
                            // Give info about error
                            req.response()
                                    .setStatusCode(500)
                                    .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                                    .end(Json.encodePrettily(new JsonObject()
                                            .put("message", messageHandler.cause().getMessage())
                                    ));
                        }
                    });
                });
            }
        }).listen(config().getInteger("http.port",7272), handler -> {
            if(handler.succeeded()) {
                System.out.println("Ditto Server Driver Http Endpoint published");
            } else {
                System.out.println("Failed to deploy Ditto Server Driver");
            }
        });
    }

    private void handleDittoWebSocket(HttpClient client) {
        String host = config().getString("ditto.http.host", "localhost");
        Integer port = config().getInteger("ditto.http.port", 8080);

        System.out.println("Ditto server: " + host);
        System.out.println("Ditto port: " + port);
        // Subscribe to ditto events and make it available to vertx event bus
        //// TODO: 5/17/18 checking if connection will be alive
        RequestOptions requestOptions = new RequestOptions()
                .setHost(host)
                .setPort(port)
                .setURI("/ws/2");
        if(port == 443 || port == 8443 || config().getBoolean("ditto.ssl", false)) {
            requestOptions.setSsl(true);
        }

        client.websocket(
                requestOptions,
                new VertxHttpHeaders()
                        .add(HttpHeaders.AUTHORIZATION, "Basic " + getAuthKey())
                ,
                this::handleWebSocketSuccess,
                error-> {
                    System.out.println("Connection to websocket failed.");
                    System.out.println(error.getMessage());
                    error.printStackTrace();
                });
    }

    private void handleWebSocketSuccess(WebSocket webSocket) {
        System.out.println("Websocket connection established");
        dittoWebSocket = webSocket;
        dittoWebSocket.handler( data -> {
            if (data.toString("ISO-8859-1").endsWith("ACK")) {
                System.out.println("Received ack ditto:: " + data.toString("ISO-8859-1"));
                if (first) {
                    first = false;
                    // When web app gets first acknowledgement, periodically sent heartbeat messages
                    // to keep connection alive
                    vertx.setPeriodic(1000, handler-> {
                        dittoWebSocket.writePing(Buffer.buffer());
                        checkPong = true;
                    });
                }
            } else if (data.toString("ISO-8859-1").equals("")) {
                lastPongTs = new Date().getTime();
            } else {
                vertx.eventBus().publish(DITTO_EVENTS, new JsonObject(data));
            }
        });

        dittoWebSocket.writeTextMessage("START-SEND-EVENTS");

        dittoWebSocket.exceptionHandler(handler -> {
            logger.error(handler.getMessage());
        });

        dittoWebSocket.closeHandler(handler -> {
            logger.warn("Websocket connection has been closed..");
        });
    }

    private void proxyDittoResponse(JsonObject dittoRes, HttpServerRequest req) {
        System.out.println("Sending response to caller");
        System.out.println(Json.encodePrettily(dittoRes));
        req.response().setChunked(true);
        JsonObject headers = dittoRes.getJsonObject("headers");
        Map<String,String> headerMap = new HashMap<>();
        for (String header:headers.fieldNames()){
            headerMap.put(header, headers.getString(header));
        }
        req.response()
                .headers().setAll(headerMap);
        req.response().setStatusCode(dittoRes.getInteger("statusCode"));
        byte[] responseBody = dittoRes.getBinary("body");
        if(responseBody != null) {
            req.response().write(Buffer.buffer(responseBody));
        }
        req.response().end();
        System.out.println("Response sent to caller..");
    }

    private void requestDittoServer(HttpClient client, JsonObject message, Handler<AsyncResult<JsonObject>> next) {

        String uri = message.getString("uri"); //resource of ditto
        String method = message.getString("method"); //request method Eg: GET, PUT, POST, DELETE, etc.
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        String host = config().getString("ditto.http.host", "localhost");
        Integer port = config().getInteger("ditto.http.port", 8080);
        boolean ssl = false;
        if (port == 443 || port == 8443) {
            ssl = true;
        }
        Buffer body = null;
        if(message.fieldNames().contains("body")) {
            body = Buffer.buffer(message.getBinary("body"));
        }

        HttpClientRequest c_req = client.request(httpMethod,
                new RequestOptions()
                        .setHost(host)
                        .setPort(port)
                        .setURI(uri)
                        .setSsl(ssl),
                c_res -> {
                    System.out.println("Proxying response: " + c_res.statusCode());
                    JsonObject response = new JsonObject();
                    response.put("statusCode", c_res.statusCode());
                    JsonObject headers = new JsonObject();
                    for (Map.Entry<String,String> entry: c_res.headers().entries()){
                        headers.put(entry.getKey(), entry.getValue());
                    }
                    response.put("headers", headers);

                    Buffer data = new BufferImpl();
                    c_res.handler(x -> data.appendBytes(x.getBytes()));
                    c_res.endHandler((v) -> {
                        response.put("body", data.getBytes());
                        System.out.println("Proxy Response Completed.");
                        next.handle(Future.succeededFuture(response));
                    });
                });
        c_req.setChunked(true);
        //Adding ditto authorization
        c_req.putHeader(HttpHeaders.AUTHORIZATION, "Basic "+getAuthKey());
        if(body != null) {
            c_req.write(body);
        }
        c_req.end();
    }

    private String getAuthKey() {
        String apiKey = config().getString("ditto.http.username", "ditto");
        String secretKey = config().getString("ditto.http.password", "ditto");
        String auth = apiKey + ":" + secretKey;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        super.stop(future);
        if(dittoWebSocket!=null) {
            System.out.println("Verticle is stopping.. Unsubscribing from ditto events");
            dittoWebSocket.writeTextMessage("STOP-SEND-EVENTS");
            dittoWebSocket.close();
        } else {
            System.out.println("Ditto websocket it null. Fix it");
        }
    }
}
