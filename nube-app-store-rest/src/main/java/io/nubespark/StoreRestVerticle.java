package io.nubespark;

import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by topsykretts on 4/28/18.
 */
public class StoreRestVerticle extends MicroServiceVerticle {

    Map<String, JsonObject> deploymentMap = new HashMap<>();
    Map<String, JsonObject> failedDeployments = new HashMap<>();

    //receiving address
    public static String ADDRESS_INSTALLER_REPORT = "io.nubespark.app.installer.report";
    private static final String ADDRESS_BIOS_REPORT = "io.nubespark.bios.report";

    //sending address
    public static String ADDRESS_EDGE_INSTALLER = "io.nubespark.app.installer";
    public static final String ADDRESS_BIOS = "io.nubespark.bios";

    Logger logger = LoggerFactory.getLogger(StoreRestVerticle.class);


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

        vertx.eventBus().consumer(ADDRESS_INSTALLER_REPORT, this::handleReports);

        vertx.eventBus().consumer(ADDRESS_BIOS_REPORT, this::handleReports);

        publishHttpEndpoint("io.nubespark.app.store.rest", "localhost", config().getInteger("http.port", 8080), ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Nube App Store (Rest endpoint) service published : " + ar.succeeded());
            }
        });

        publishMessageSource(ADDRESS_BIOS_REPORT, ADDRESS_BIOS_REPORT, ar->{
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Nube Bios Report (Message source) published : " + ar.succeeded());
            }
        });

        publishMessageSource(ADDRESS_INSTALLER_REPORT, ADDRESS_INSTALLER_REPORT, ar-> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Nube App Installer Report (Message source) published : " + ar.succeeded());
            }
        });
    }

    private void handleReports(Message<Object> message) {
        JsonObject msg = new JsonObject(message.body().toString());
        String status = message.headers().get("status");
        String serviceName = msg.getString("serviceName");
        if("INSTALLED".equals(status) || "UPDATED".equals(status)) {
            logger.info("Received install success message ", Json.encodePrettily(msg));
            deploymentMap.put(serviceName, msg);
        } else if("UNINSTALLED".equals(status)) {
            logger.info("Received uninstall success message ", serviceName);
            deploymentMap.remove(serviceName);
        } else {
            failedDeployments.put(serviceName, msg);
        }
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message.
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                    .end(Json.encodePrettily(new JsonObject()
                            .put("name", "nubespark app store REST API")
                            .put("version", "1.0")
                            .put("vert.x_version", "3.4.1")
                            .put("java_version", "8.0")
                    ));
        });

        //// TODO: 4/26/18 other routing logic here
        router.route("/api/store*").handler(BodyHandler.create());
        router.post("/api/store/install").handler(routingContext -> install(routingContext, "install"));
        router.post("/api/store/uninstall").handler(routingContext -> install(routingContext, "uninstall"));
        router.post("/api/store/os").handler(this::installOS);
        router.get("/api/store/deployments").handler(this::getDeployments);

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

    private void installOS(RoutingContext routingContext) {
        JsonObject reqBody = routingContext.getBodyAsJson();
        String version = reqBody.getString("version");
        JsonObject options = reqBody.getJsonObject("options", new JsonObject());
        vertx.eventBus().publish(ADDRESS_BIOS, new JsonObject().put("version", version).put("options", options));
        logger.info(Json.encodePrettily(new JsonObject().put("version", version).put("options", options)));
        routingContext.response()
                .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(new JsonObject()
                        .put("action", "update")
                        .put("body", reqBody)
                        .put("status", "PUBLISHED")));
    }

    private void install(RoutingContext routingContext, String action) {
            JsonObject reqBody = routingContext.getBodyAsJson();
            System.out.println(Json.encodePrettily(reqBody));
            vertx.eventBus().publish(ADDRESS_EDGE_INSTALLER, reqBody, new DeliveryOptions().addHeader("action", action));
            routingContext.response()
                    .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                    .end(Json.encodePrettily(new JsonObject()
                    .put("action", action)
                    .put("body", reqBody)
                    .put("status", "PUBLISHED")
            ));



    }

    private void getDeployments(RoutingContext routingContext) {
        routingContext.response()
                .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(deploymentMap));
    }

    private void handleFailure(AsyncResult handler) {
        logger.error(handler.cause().getMessage());
        Future.failedFuture(handler.cause());
    }
}
