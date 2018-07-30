package io.nubespark;

import io.nubespark.controller.MongoDBController;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.StaticHandler;

public class MongoDBVerticle extends MicroServiceVerticle {
    private int DEFAULT_PORT = 8083;

    private MongoDBController controller;
    private MongoClient client;

    @Override
    public void start() {
        super.start();
        System.out.println("Config on MongoDB");
        System.out.println(Json.encodePrettily(config()));

        client = MongoClient.createNonShared(vertx, config());

        handleRESTfulRequest(http -> {
            if (http.succeeded()) {
                System.out.println("Server started");
            } else {
                System.out.println("Cannot start the server: " + http.cause());
            }
        });
    }

    private void handleRESTfulRequest(Handler<AsyncResult<HttpServer>> next) {
        controller = new MongoDBController(client);

        OpenAPI3RouterFactory.create(this.vertx, "/webroot/apidoc/nube-vertx-mongodb.json",
                ar -> {
                    System.out.println("Handler of OpenAPI3RouterFactory is called...");
                    if (ar.succeeded()) {
                        System.out.println("Success of OpenAPI3RouterFactory");
                        OpenAPI3RouterFactory routerFactory = ar.result();

                        // Enable automatic response when ValidationException is thrown
                        RouterFactoryOptions options =
                                new RouterFactoryOptions()
                                        .setMountNotImplementedHandler(true)
                                        .setMountValidationFailureHandler(true);

                        routerFactory.setOptions(options);

                        // Add routes handlers
                        routerFactory.addHandlerByOperationId("/get/:document", routingContext -> controller.getAll(routingContext));
                        routerFactory.addHandlerByOperationId("/get/:document/:id", routingContext -> controller.getOne(routingContext));
                        routerFactory.addHandlerByOperationId("/save/:document", routingContext -> controller.save(routingContext));
                        routerFactory.addHandlerByOperationId("/delete/:document", routingContext -> controller.deleteAll(routingContext));
                        routerFactory.addHandlerByOperationId("/delete/:document/:id", routingContext -> controller.deleteOne(routingContext));

                        // Generate the router
                        Router router = routerFactory.getRouter();
                        router.route("/*").handler(StaticHandler.create());
                        router.route().last().handler(routingContext -> {
                            if (routingContext.response().getStatusCode() == 404) {
                                System.out.println("Resource Not Found");
                            }
                            routingContext.response()
                                    .setStatusCode(404)
                                    .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                                    .end(Json.encodePrettily(new JsonObject()
                                            .put("message", "Resource Not Found")
                                    ));
                        });

                        HttpServer server = vertx.createHttpServer(new HttpServerOptions()
                                .setPort(config().getInteger("http.port", DEFAULT_PORT))
                        );
                        server.requestHandler(router::accept).listen();
                        next.handle(Future.succeededFuture(server));
                    } else {
                        System.out.println("Failure in OpenAPI3RouterFactory");
                        next.handle(Future.failedFuture(ar.cause()));
                    }
                });

        publishHttpEndpoint("mongodb-api",
                config().getString("http.host", "0.0.0.0"),
                config().getInteger("http.port", DEFAULT_PORT),
                ar -> {
                    if (ar.succeeded()) {
                        System.out.println("MongoDB REST endpoint published successfully..");
                    } else {
                        System.out.println("Failed to publish MongoDB REST endpoint");
                        ar.cause().printStackTrace();
                    }
                }
        );
    }
}
