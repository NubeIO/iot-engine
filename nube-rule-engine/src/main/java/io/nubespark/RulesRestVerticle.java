package io.nubespark;

import io.nubespark.controller.RulesController;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by topsykretts on 4/26/18.
 */
public class RulesRestVerticle extends MicroServiceVerticle {

    RulesController controller;

    @Override
    public void start() {
        super.start();
        startWebApp(http -> {
            if (http.succeeded()) {
                System.out.println("Server started");
            } else {
                System.out.println("Cannot start the server: " + http.cause());
            }
        });
        publishHttpEndpoint("io.nubespark.rule.engine", "localhost", config().getInteger("http.port", 8080), ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Rule Engine (Rest endpoint) service published : " + ar.succeeded());
            }
        });
        controller = new RulesController(vertx);
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message.
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new JsonObject()
                            .put("name", "nubespark rule engine")
                            .put("version", "1.0")
                            .put("vert.x_version", "3.4.1")
                            .put("java_version", "8.0")
                    ));
        });

        //// TODO: 4/26/18 other routing logic here
        router.route("/api/rule*").handler(BodyHandler.create());
        router.get("/api/rule").handler(routingContext -> controller.getAll(routingContext));
        router.get("/api/rule/:id").handler(routingContext -> controller.getOne(routingContext));

        // This is last handler that gives not found message
        router.route().last().handler(routingContext -> {
            String uri = routingContext.request().absoluteURI();
            routingContext.response()
                    .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                    .setStatusCode(404)
                    .end(Json.encodePrettily(new JsonObject()
                            .put("uri", uri)
                            .put("status", 404)
                            .put("message", "Resource Not Found")
                    ));
        });

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", 8080),
                        next::handle
                );
    }
}
