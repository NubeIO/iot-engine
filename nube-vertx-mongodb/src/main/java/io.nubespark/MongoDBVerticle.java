package io.nubespark;

import io.nubespark.controller.MongoDBController;
import io.nubespark.utils.Runner;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import static io.nubespark.constants.Port.MONGO_DB_PORT;

public class MongoDBVerticle extends MicroServiceVerticle {
    private MongoDBController controller;
    private MongoClient client;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-vertx-mongodb/src/main/java";
        Runner.runExample(JAVA_DIR, MongoDBVerticle.class);
    }

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

        Router router = Router.router(vertx);
        // creating body handler
        router.route().handler(BodyHandler.create());

        router.get("/get/:document").handler(routingContext -> controller.getAll(routingContext));
        router.post("/get/:document").handler(routingContext -> controller.getAll(routingContext));
        router.get("/get/:document/:id").handler(routingContext -> controller.getOne(routingContext));
        router.post("/post/:document").handler(routingContext -> controller.post(routingContext)); // will throw 409 status code if same id is already there
        router.put("/put/:document").handler(routingContext -> controller.put(routingContext)); // will override if same id is there
        router.delete("/delete/:document").handler(routingContext -> controller.deleteAll(routingContext));
        router.post("/delete/:document").handler(routingContext -> controller.deleteAll(routingContext));
        router.delete("/delete/:document/:id").handler(routingContext -> controller.deleteOne(routingContext));

        // This is last handler that gives not found message
        router.route().last().handler(routingContext -> {
            String uri = routingContext.request().absoluteURI();
            routingContext.response()
                .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .setStatusCode(404)
                .end(Json.encodePrettily(new JsonObject()
                    .put("uri", uri)
                    .put("status", 404)
                    .put("message", "Resource Not Found!")
                ));
        });

        // Create the HTTP server and pass the "accept" method to the request handler
        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(
                // Retrieve the port from the configuration
                config().getInteger("http.port", MONGO_DB_PORT), next);

        publishHttpEndpoint("mongodb-api",
            config().getString("http.host", "0.0.0.0"),
            config().getInteger("http.port", MONGO_DB_PORT),
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
