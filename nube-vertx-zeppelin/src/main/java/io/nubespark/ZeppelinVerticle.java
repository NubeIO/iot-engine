package io.nubespark;

import io.nubespark.utils.Runner;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

import static io.nubespark.constants.Port.ZEPPELIN_PORT;

public class ZeppelinVerticle extends MicroServiceVerticle {

    private HttpClient client;
    private String COOKIE_NAME = "JSESSIONID";

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-vertx-zeppelin/src/main/java/";
        Runner.runExample(JAVA_DIR, ZeppelinVerticle.class);
    }

    @Override
    public void start() {
        super.start();

        client = vertx.createHttpClient(new HttpClientOptions());

        System.out.println("Config on Zeppelin");
        System.out.println(Json.encodePrettily(config()));

        Router router = Router.router(vertx);
        // creating body handler
        router.route().handler(BodyHandler.create());
        handleRoutes(router);

        HttpServer server = vertx.createHttpServer(new HttpServerOptions()
            .setPort(config().getInteger("http.port", ZEPPELIN_PORT))
        );
        server.requestHandler(router::accept).listen();

        publishHttpEndpoint("zeppelin-api",
            config().getString("http.host", "0.0.0.0"),
            config().getInteger("http.port", ZEPPELIN_PORT),
            ar -> {
                if (ar.succeeded()) {
                    System.out.println("Zeppelin REST endpoint published successfully..");
                } else {
                    System.out.println("Failed to publish Zeppelin REST endpoint");
                    ar.cause().printStackTrace();
                }
            }
        );
    }

    private void handleRoutes(Router router) {
        // api dispatcher
        router.route("/api/*").handler(this::dispatchRequests);
    }

    private void dispatchRequests(RoutingContext context) {
        HttpClientRequest toReq = client.request(context.request().method(),
            config().getInteger("server.port"), config().getString("server.host"), context.request().uri(), response -> {
                response.bodyHandler(body -> {
                    HttpServerResponse toRsp = context.response().setStatusCode(response.statusCode());
                    if (response.statusCode() < 500) {
                        response.headers().forEach(header -> {
                            if (!header.getKey().equalsIgnoreCase("Set-Cookie")) {
                                // Ignore cookies; on login it will send cookie
                                toRsp.putHeader(header.getKey(), header.getValue());
                            }
                        });
                        body = cookieHandler(response, toRsp, body);
                    }
                    toRsp.end(body);
                });
            });
        // set headers
        context.request().headers().forEach(header -> {
            if (header.getKey().equalsIgnoreCase(COOKIE_NAME)) {
                // Sending as an cookie for authentication parameter
                toReq.putHeader("Cookie", header.getValue());
            } else {
                toReq.putHeader(header.getKey(), header.getValue());
            }
        });

        if (context.getBody() == null) {
            toReq.end();
        } else {
            toReq.end(context.getBody());
        }
    }

    private Buffer cookieHandler(HttpClientResponse response, HttpServerResponse toRsp, Buffer body) {
        final Map<String, String> cookie = new HashMap<>();
        response.headers().forEach(header -> {
            if (header.getKey().equalsIgnoreCase("Set-Cookie")) {
                if (header.getValue().contains(COOKIE_NAME)) {
                    // Two JSESSIONID= will be available and we need the last one
                    cookie.put(COOKIE_NAME, (header.getValue().split(";")[0]).split("=")[1]);
                }
            }
        });
        if (body.length() != 0) {
            System.out.println(new JsonObject(body).toString());
            if (cookie.keySet().contains(COOKIE_NAME)) {
                JsonObject responseJsonObject = new JsonObject(body);
                JsonObject body$ = responseJsonObject.getJsonObject("body").put(COOKIE_NAME, cookie.get(COOKIE_NAME));
                responseJsonObject = responseJsonObject.put("body", body$);
                body = responseJsonObject.toBuffer();
                toRsp.putHeader("Content-Length", Integer.toString(body.toString().toCharArray().length));
            }
        }
        return body;
    }
}
