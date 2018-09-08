package io.nubespark;

import io.nubespark.utils.Runner;
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

import static io.nubespark.constants.Port.SERVER_DITTO_DRIVER_PORT;


/**
 * Created by topsykretts on 5/11/18.
 */
public class ServerDittoDriver extends MicroServiceVerticle {

    private static final String SERVER_DITTO_DRIVER = "io.nubespark.server.ditto.driver";
    private static final String DITTO_EVENTS = "io.nubespark.ditto.events";

    private Logger logger = LoggerFactory.getLogger(ServerDittoDriver.class);

    private WebSocket dittoWebSocket;
    private HttpClient client;
    private long failedTs = new Date().getTime();

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-server-ditto-driver/src/main/java/";
        Runner.runExample(JAVA_DIR, ServerDittoDriver.class);
    }

    @Override
    public void start() {
        super.start();

        client = vertx.createHttpClient(new HttpClientOptions()
            .setVerifyHost(false)
            .setTrustAll(true)
            .setTcpKeepAlive(true)
        );

        handleDittoWebSocket(client);

        /**
         * Handling web request: GET, PUT (mainly), POST (doesn't fit here, since only command will be executed from here)
         * GET: for getting details
         * PUT: for sending command to WebApplication -> WebServer -> ServerDittoDriver -> EdgeDittoDriver ->
         * ServerDittoDriver -> Ditto -> ServerDittoDriver -> WebServer -> WebApplication
         */
        handleWebServer(client);

        publishMessageSource(SERVER_DITTO_DRIVER, SERVER_DITTO_DRIVER, ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Published Ditto Server message source.");
            }
        });

        publishMessageSource(DITTO_EVENTS, DITTO_EVENTS, ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Published Ditto Events message source.");
            }
        });

        // This is message received from Edge ditto driver
        MessageSource.<JsonObject>getConsumer(discovery, new JsonObject().put("name", SERVER_DITTO_DRIVER), ar -> {
            if (ar.failed()) {
                logger.error("Message source {} is not discovered.", SERVER_DITTO_DRIVER);
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
    }


    private void handleWebServer(HttpClient client) {
        vertx.createHttpServer().requestHandler(req -> {
            JsonObject request = new JsonObject();
            request.put("method", req.method().toString());
            request.put("uri", req.uri());

            if (req.method().equals(HttpMethod.GET)) {
                System.out.println("Proxying request: " + req.uri());
                requestDittoServer(client, request, dittoResHandler -> {
                    JsonObject dittoRes = dittoResHandler.result();
                    proxyDittoResponse(dittoRes, req);
                });
            } else {
                req.bodyHandler(body -> {
                    JsonObject decodedRequest = new JsonObject(request.toString());
                    if (body != null) {
                        request.put("body", body.getBytes());
                        decodedRequest.put("body", new String(body.getBytes()));
                    }
                    System.out.println(Json.encodePrettily(request));
                    //Execute actual request...
                    requestDittoServer(client, request, dittoResHandler -> {
                        JsonObject dittoRes = dittoResHandler.result();
                        System.out.println("Got response from ditto");
                        proxyDittoResponse(dittoRes, req);
                    });
                });
            }
        }).listen(config().getInteger("http.port", SERVER_DITTO_DRIVER_PORT), handler -> {
            if (handler.succeeded()) {
                System.out.println("Ditto Server Driver Http Endpoint published");
                publishHttpEndpoint("ditto-api",
                    config().getString("http.host", "0.0.0.0"),
                    config().getInteger("http.port", SERVER_DITTO_DRIVER_PORT),
                    ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Ditto Server Driver Http Endpoint published");
                        } else {
                            System.out.println("Failed to publish Ditto Server Driver Http Endpoint");
                            ar.cause().printStackTrace();
                        }
                    }
                );
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
        RequestOptions requestOptions = new RequestOptions()
            .setHost(host)
            .setPort(port)
            .setURI("/ws/2");
        if (port == 443 || port == 8443 || config().getBoolean("ditto.ssl", false)) {
            requestOptions.setSsl(true);
        }

        client.websocket(
            requestOptions,
            new VertxHttpHeaders()
                .add(HttpHeaders.AUTHORIZATION, "Basic " + getAuthKey())
            ,
            this::handleWebSocketSuccess,
            error -> {
                // Reconnects on every 5 seconds on failure...
                vertx.setTimer(5000, l -> handleDittoWebSocket(client));
                if (failedTs != 0) {
                    long now = new Date().getTime();
                    long diff = now - failedTs;
                    System.out.println("Ditto Websocket is disconnected for: " + diff);
                }
                System.out.println("Connection to websocket failed.");
                System.out.println(error.getMessage());
                System.out.println("Retrying to connect...");
                error.printStackTrace();
            });
    }

    private void handleWebSocketSuccess(WebSocket webSocket) {
        System.out.println("Websocket connection established");
        dittoWebSocket = webSocket;
        dittoWebSocket.handler(data -> {
            if (data.toString("ISO-8859-1").endsWith("ACK")) {
                System.out.println("Received ack ditto::: " + data.toString("ISO-8859-1"));
            } else if (!data.toString("ISO-8859-1").equals("")) {
                vertx.eventBus().publish(DITTO_EVENTS, new JsonObject(data));
            }
        });

        dittoWebSocket.writeTextMessage("START-SEND-EVENTS");

        dittoWebSocket.exceptionHandler(handler -> logger.error(handler.getMessage()));

        dittoWebSocket.closeHandler(handler -> {
            logger.warn("Websocket connection has been closed...");
            failedTs = new Date().getTime();
            // Reconnects on every 5 seconds on Websocket close...
            vertx.setTimer(5000, l -> handleDittoWebSocket(client));
        });
    }

    private void proxyDittoResponse(JsonObject dittoRes, HttpServerRequest req) {
        req.response().setChunked(true);
        JsonObject headers = dittoRes.getJsonObject("headers");
        Map<String, String> headerMap = new HashMap<>();
        for (String header : headers.fieldNames()) {
            headerMap.put(header, headers.getString(header));
        }
        req.response()
            .headers().setAll(headerMap);
        req.response().setStatusCode(dittoRes.getInteger("statusCode"));
        byte[] responseBody = dittoRes.getBinary("body");
        if (responseBody != null) {
            req.response().write(Buffer.buffer(responseBody));
        }
        req.response().end();
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
        if (message.fieldNames().contains("body")) {
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
                for (Map.Entry<String, String> entry : c_res.headers().entries()) {
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
        c_req.putHeader(HttpHeaders.AUTHORIZATION, "Basic " + getAuthKey());
        if (body != null) {
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
        if (dittoWebSocket != null) {
            System.out.println("Verticle is stopping... Un-subscribing from ditto events");
            dittoWebSocket.writeTextMessage("STOP-SEND-EVENTS");
            dittoWebSocket.close();
        } else {
            System.out.println("Ditto websocket it null. Fix it");
        }
    }
}
