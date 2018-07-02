package io.nubespark;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.Address;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by topsykretts on 5/12/18.
 */
public class EdgeDittoDriver extends MicroServiceVerticle {

    private static final String EDGE_DITTO_DRIVER = "io.nubespark.edge.ditto.driver";
    private static final String SERVER_DITTO_DRIVER = "io.nubespark.server.ditto.driver";

    @Override
    public void start() {
        super.start();
        String nodeRedHost = config().getString("nodered.host", "localhost");
        Integer nodeRedPort = config().getInteger("nodered.port", 1880);
        System.out.println("NodeRED Host:" + nodeRedHost);
        System.out.println("NodeRED Port:" + nodeRedPort);

        HttpClient client = vertx.createHttpClient(new HttpClientOptions());

        // from edge to node-red
        vertx.eventBus().<JsonObject>consumer(EDGE_DITTO_DRIVER, message -> {
            JsonObject request = message.body();
            System.out.println("Received request from server...");

            HttpClientRequest c_req = client.request(HttpMethod.POST,
                    nodeRedPort,
                    nodeRedHost,
                    "/device/manager",
                    c_res -> {
                        JsonObject response = new JsonObject();
                        response.put("statusCode", c_res.statusCode());
                        JsonObject headers = new JsonObject();
                        for (Map.Entry<String, String> entry : c_res.headers().entries()) {
                            headers.put(entry.getKey(), entry.getValue());
                        }
                        response.put("headers", headers);
                        c_res.handler(data -> response.put("body", data.getBytes()));
                        c_res.endHandler((v) -> {
                            System.out.println("Got response from NodeRED. Sending it to server.");
                            message.reply(response);
                        });
                    });
            c_req.setChunked(true);
            // Adding ditto authorization
            c_req.write(Buffer.buffer(Json.encodePrettily(request)));

            c_req.end();
            System.out.println("Requesting NodeRED to handle the request from server...");
        });

        // TODO: Since we are doing same this work from HttpServerVerticle with Auth protection, we don't need this one.
        vertx.createHttpServer().requestHandler(req -> {
            JsonObject request = new JsonObject();
            request.put("method", req.method().toString());
            request.put("uri", req.uri());
            req.bodyHandler(body -> {
                System.out.println("Inside body handler...");
                if (body != null) {
                    request.put("body", body.getBytes());
                }
                System.out.println(Json.encodePrettily(request));
                System.out.println("Sending response...");
                vertx.eventBus().<JsonObject>send(SERVER_DITTO_DRIVER, request, handler -> {
                    if (handler.succeeded()) {
                        JsonObject response = handler.result().body();
                        if (req.method() != HttpMethod.GET) {
                            req.response().setChunked(true);
                        }
                        JsonObject headers = response.getJsonObject("headers");
                        Map<String, String> headerMap = new HashMap<>();
                        for (String header : headers.fieldNames()) {
                            headerMap.put(header, headers.getString(header));
                        }
                        req.response()
                                .headers().setAll(headerMap);
                        req.response().setStatusCode(response.getInteger("statusCode"));
                        byte[] responseBody = response.getBinary("body");
                        if (responseBody != null) {
                            req.response().write(Buffer.buffer(responseBody));
                        }
                        req.response().end();
                    } else {
                        // TODO: 5/12/18 Identify cases where request fails and handle accordingly
                        req.response()
                                .setStatusCode(500)
                                .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                                .end(Json.encodePrettily(new JsonObject()
                                        .put("message", "Internal Server Error")
                                        .put("error", handler.cause().getMessage())
                                ));
                    }
                });
                System.out.println(" After sending response..");
            });
            System.out.println("Outside body handler..");
        }).listen(config().getInteger("http.port", 7171), handler -> {
            if (handler.succeeded()) {
                System.out.println("Ditto Edge Driver Http Endpoint published");
            } else {
                System.out.println("Failed to deploy Ditto Server Driver");
            }
        });
    }

    private String getDeviceAddress() {
        for (HazelcastInstance instance : Hazelcast.getAllHazelcastInstances()) {
            Address address = instance.getCluster().getLocalMember().getAddress();
            return address.getHost() + ":" + address.getPort();
        }
        return null;
    }
}
