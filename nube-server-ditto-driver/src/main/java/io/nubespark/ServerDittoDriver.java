package io.nubespark;

import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by topsykretts on 5/11/18.
 */
public class ServerDittoDriver extends MicroServiceVerticle {

    private static final String EDGE_DITTO_DRIVER = "io.nubespark.edge.ditto.driver";
    private static final String SERVER_DITTO_DRIVER = "io.nubespark.server.ditto.driver";

    @Override
    public void start() {
        super.start();

        HttpClient client = vertx.createHttpClient(new HttpClientOptions());

        //This is message received from Edge ditto driver
        vertx.eventBus().<JsonObject>consumer(SERVER_DITTO_DRIVER, message -> {
            System.out.println("Received message:: " + Json.encodePrettily(message.body()));
            // Request ditto server and send response ...
            requestDittoServer(client, message.body(), dittoResHandler -> {
                JsonObject dittoResponse = dittoResHandler.result();
                message.reply(dittoResponse);
            });

        });

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
                    String edgeAddress = EDGE_DITTO_DRIVER;
                    System.out.println("Forwarding request to check with edge driver");
                    vertx.eventBus().send(edgeAddress, request, messageHandler -> {
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
        Buffer body = null;
        if(message.fieldNames().contains("body")) {
            body = Buffer.buffer(message.getBinary("body"));
        }

        HttpClientRequest c_req = client.request(httpMethod,
                config().getInteger("ditto.http.port", 7171),
                config().getString("ditto.http.host", "localhost"),
                uri,
                c_res -> {
                    System.out.println("Proxying response: " + c_res.statusCode());
                    JsonObject response = new JsonObject();
                    response.put("statusCode", c_res.statusCode());
                    JsonObject headers = new JsonObject();
                    for (Map.Entry<String,String> entry: c_res.headers().entries()){
                        headers.put(entry.getKey(), entry.getValue());
                    }
                    response.put("headers", headers);

                    c_res.handler(data -> {
//                        System.out.println("Proxying response body: " + data.toString("ISO-8859-1"));
                        response.put("body", data.getBytes());
                    });
                    c_res.endHandler((v) -> {
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
}
