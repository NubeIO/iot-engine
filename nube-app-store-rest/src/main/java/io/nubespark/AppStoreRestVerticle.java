package io.nubespark;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.utils.CustomMessage;
import io.nubespark.utils.CustomMessageCodec;
import io.nubespark.utils.Runner;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.RxRestAPIVerticle;
import io.reactivex.Single;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.nubespark.constants.Port.APP_STORE_PORT;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

/**
 * Created by topsykretts on 4/28/18.
 */
public class AppStoreRestVerticle extends RxRestAPIVerticle {
    // sending address
    private static String ADDRESS_APP_INSTALLER = "io.nubespark.app.installer";

    private Logger logger = LoggerFactory.getLogger(AppStoreRestVerticle.class);
    private EventBus eventBus;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-app-store-rest/src/main/java/";
        Runner.runExample(JAVA_DIR, AppStoreRestVerticle.class);
    }

    @Override
    public void start(io.vertx.core.Future<Void> future) {
        super.start();

        eventBus = getVertx().eventBus();
        // Register codec for custom message
        eventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());

        logger.info("Config on app store REST");
        logger.info(Json.encodePrettily(config()));

        startWebApp()
            .flatMap(httpServer -> publishHttp())
            .subscribe(ignored -> future.complete(), future::fail);
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint("io.nubespark.sql-hive.engine", "0.0.0.0", config().getInteger("http.port", APP_STORE_PORT))
            .doOnError(throwable -> logger.error("Cannot publish: " + throwable.getLocalizedMessage()));
    }

    private Single<HttpServer> startWebApp() {
        // Create a router object.
        Router router = Router.router(vertx);

        // creating body handler
        router.route("/").handler(this::indexHandler);
        router.route().handler(BodyHandler.create());
        router.post("/install").handler(ctx -> install(ctx, "install"));
        router.post("/uninstall").handler(ctx -> install(ctx, "uninstall"));
        router.get("/nodes").handler(this::getNodes);
        // This is last handler that gives not found message
        router.route().last().handler(this::handlePageNotFound);

        // Create the HTTP server and pass the "accept" method to the request handler.
        return createHttpServer(router, config().getString("http.host", "0.0.0.0"), config().getInteger("http.port", APP_STORE_PORT))
            .doOnSuccess(httpServer -> logger.info("Web Server started at " + httpServer.actualPort()))
            .doOnError(throwable -> logger.error("Cannot start server: " + throwable.getLocalizedMessage()));
    }

    private void getNodes(RoutingContext ctx) {
        List<JsonObject> nodesInfo = new ArrayList<>();
        for (HazelcastInstance instance : Hazelcast.getAllHazelcastInstances()) {
            JsonObject info = new JsonObject();
            info.put("instance", instance.getName());
            List<String> members = instance.getCluster().getMembers()
                .stream()
                .map(member -> member.getAddress().getHost() + ":" + member.getAddress().getPort())
                .collect(Collectors.toList());
            info.put("members", new JsonArray(members));
            nodesInfo.add(info);
        }

        ctx.response()
            .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
            .putHeader("Access-Control-Allow-Origin", "*")
            .end(Json.encodePrettily(new JsonArray(nodesInfo)));
    }


    private void install(RoutingContext ctx, String action) {
        JsonObject body = ctx.getBodyAsJson();
        JsonObject header = new JsonObject().put("action", action);
        logger.info(Json.encodePrettily(body));
        CustomMessage<JsonObject> message = new CustomMessage<>(header, body, 200);

        eventBus.send(ADDRESS_APP_INSTALLER, message, reply -> {
            if (reply.succeeded()) {
                CustomMessage replyMessage = (CustomMessage) reply.result().body();
                logger.info("Received reply: " + replyMessage.getBody());
                ctx.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(replyMessage.getStatusCode())
                    .end(replyMessage.getBody().toString());
            } else {
                ctx.response().setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end();
                logger.info("No reply from cluster receiver");
            }
        });
    }

    private void handlePageNotFound(RoutingContext routingContext) {
        String uri = routingContext.request().absoluteURI();
        routingContext.response()
            .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
            .setStatusCode(404)
            .end(Json.encodePrettily(new JsonObject()
                .put("uri", uri)
                .put("status", 404)
                .put("message", "Resource Not Found")
            ));
    }

    // Returns verticle properties in json
    private void indexHandler(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        response.putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(new JsonObject()
                .put("name", "app-store-rest")
                .put("version", "1.0")
                .put("vert.x_version", "3.4.1")
                .put("java_version", "8.0")
            ));
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
