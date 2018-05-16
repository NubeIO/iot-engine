package io.nubespark;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.FailedFuture;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        System.out.println("Config on app store REST");
        System.out.println(Json.encodePrettily(config()));
        startWebApp(http -> {
            if (http.succeeded()) {
                System.out.println("Server started");
            } else {
                System.out.println("Cannot start the server: " + http.cause());
            }
        });

        vertx.eventBus().consumer(ADDRESS_INSTALLER_REPORT, this::handleReports);

        vertx.eventBus().consumer(ADDRESS_BIOS_REPORT, this::handleReports);

        //// TODO: 5/16/18 get public host name and add an API to get available services
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
        System.out.println("Starting web app from Open API 3 Specification..");
        OpenAPI3RouterFactory.create(this.vertx, "/webroot/apidoc/nube-app-store.json", openAPI3RouterFactoryAsyncResult -> {
            System.out.println("Handler of openAPI3RouterFactory called...");
            if (openAPI3RouterFactoryAsyncResult.succeeded()) {
                System.out.println("Success of OpenAPI3RouterFactory");
                OpenAPI3RouterFactory routerFactory = openAPI3RouterFactoryAsyncResult.result();

                // Enable automatic response when ValidationException is thrown
                RouterFactoryOptions options =
                        new RouterFactoryOptions()
                                .setMountNotImplementedHandler(true)
                                .setMountValidationFailureHandler(true);

                routerFactory.setOptions(options);


                // Add routes handlers
                routerFactory.addHandlerByOperationId("installApp", routingContext -> install(routingContext, "install"));
                routerFactory.addHandlerByOperationId("uninstallApp", routingContext -> install(routingContext, "uninstall"));
                routerFactory.addHandlerByOperationId("upgradeOs", this::installOS);
                routerFactory.addHandlerByOperationId("getNodes", this::getNodes);


                // Generate the router
                Router router = routerFactory.getRouter();

                router.route().handler(StaticHandler.create());

                router.route().last().handler(routingContext -> {
                    if(routingContext.response().getStatusCode() == 404) {
                        System.out.println("Resource Not Found..");
                    }
                   routingContext.response()
                           .setStatusCode(404)
                           .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                           .end(Json.encodePrettily(new JsonObject()
                                   .put("message", "Resource Not Found")
                           ));
                });

                HttpServer server = vertx.createHttpServer(new HttpServerOptions()
                        .setPort(config().getInteger("http.port", 3031))
//                        .setHost(config().getString("http.host", "localhost"))
                );
                server.requestHandler(router::accept).listen();
                next.handle(Future.succeededFuture(server));
            } else {
                System.out.println("Failure in OpenAPI3RouterFactory");
                next.handle(Future.failedFuture(openAPI3RouterFactoryAsyncResult.cause()));
            }
        });
    }

    private void getNodes(RoutingContext routingContext) {

        List<JsonObject> nodesInfo = new ArrayList<>();
        for(HazelcastInstance instance:Hazelcast.getAllHazelcastInstances()) {
            JsonObject info = new JsonObject();
            info.put("instance", instance.getName());
            List<String> members = instance.getCluster().getMembers()
                    .stream()
                    .map(member -> member.getAddress().getHost() + ":" + member.getAddress().getPort())
                    .collect(Collectors.toList());
            info.put("members", new JsonArray(members));
            nodesInfo.add(info);
        }

        routingContext.response()
                .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(new JsonArray(nodesInfo)));

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
