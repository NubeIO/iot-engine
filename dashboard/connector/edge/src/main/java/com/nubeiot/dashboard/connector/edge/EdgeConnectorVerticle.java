package com.nubeiot.dashboard.connector.edge;

import java.util.List;
import java.util.stream.Collectors;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.nubeiot.core.DevRunner;
import com.nubeiot.core.common.constants.Port;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.RestUtils;
import com.nubeiot.core.common.RxRestAPIVerticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import com.nubeiot.core.common.utils.Runner;
import com.nubeiot.core.common.utils.response.ResponseUtils;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public final class EdgeConnectorVerticle extends RxRestAPIVerticle {

    //Should from config
    private static final String ROOT_API_ENDPOINT = "/api";
    private static final String HIVE_ENGINE_SERVICE = "io.nubespark.sql-hive.engine";
    private static final String PUBLIC_HOST = "0.0.0.0";

    public static void main(String[] args) {
        String JAVA_DIR = "nube-app-store-rest/src/main/java/";
        DevRunner.run(JAVA_DIR, EdgeConnectorVerticle.class);
    }

    @Override
    public void start(io.vertx.core.Future<Void> future) {
        super.start();
        logger.info("Config on app store REST {}", this.appConfig);
        String host = this.appConfig.getString("http.host", PUBLIC_HOST);
        int port = this.appConfig.getInteger("http.port", Port.APP_STORE_PORT);
        startWebApp(host, port).flatMap(httpServer -> publishHttpEndpoint(HIVE_ENGINE_SERVICE, host, port))
                               .subscribe(ignored -> future.complete(), future::fail);
    }

    private Single<HttpServer> startWebApp(String host, int port) {
        Single<HttpServer> server = createHttpServer(initRouter(), host, port);
        return server.doOnSuccess(httpServer -> logger.info("Web Server started at {}", httpServer.actualPort()))
                     .doOnError(throwable -> logger.error("Cannot start server", throwable));
    }

    private Router initRouter() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route(ROOT_API_ENDPOINT).handler(this::registerIndexHandler);
        final List<EdgeRouter.Metadata> appRouters = new EdgeRouter().init().getRouters();
        appRouters.parallelStream()
                  .forEach(metadata -> router.route(metadata.getMethod(), ROOT_API_ENDPOINT + metadata.getPath())
                                             .handler(ctx -> registerAppControllerHandler(ctx, metadata)));
        router.get(ROOT_API_ENDPOINT + "/nodes").handler(this::registerClusterNodesHandler);
        router.get(ROOT_API_ENDPOINT + "/status").handler(this::registerBiosStatusHandler);
        router.route().last().handler(this::registerNotFoundHandler);
        return router;
    }

    private void registerBiosStatusHandler(RoutingContext ctx) {
        ctx.response()
           .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
           .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
           .end(new ErrorMessage(NubeException.ErrorCode.SERVICE_ERROR, "Show BIOS status").toJson().encodePrettily());
    }

    private void registerClusterNodesHandler(RoutingContext ctx) {
        JsonArray nodesInfo = Hazelcast.getAllHazelcastInstances()
                                       .parallelStream()
                                       .map(this::getNodeInfo)
                                       .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        ctx.response()
           .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
           .end(nodesInfo.encodePrettily());
    }

    private JsonObject getNodeInfo(HazelcastInstance instance) {
        JsonObject object = new JsonObject().put("instance", instance.getName());
        return object.put("members", instance.getCluster()
                                             .getMembers()
                                             .stream()
                                             .map(member -> member.getAddress().getHost() + ":" +
                                                            member.getAddress().getPort())
                                             .collect(Collectors.toList()));
    }

    private void registerAppControllerHandler(RoutingContext ctx, EdgeRouter.Metadata metadata) {
        EventMessage msg = EventMessage.success(metadata.getAction(), RestUtils.convertToRequestData(ctx));
        logger.info("Receive message from endpoint: {}", msg.toJson().encode());
        getVertx().eventBus().send(metadata.getAddress(), msg.toJson(), reply -> handleReply(ctx, metadata, reply));
    }

    private void handleReply(RoutingContext ctx, EdgeRouter.Metadata metadata, AsyncResult<Message<Object>> reply) {
        if (reply.succeeded()) {
            EventMessage replyMsg = EventMessage.from(reply.result().body());
            logger.info("Receive message from backend: {}", replyMsg.toJson().encode());
            ctx.response().putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON);
            if (replyMsg.isSuccess()) {
                ctx.response()
                   .setStatusCode(HttpStatusMapping.success(metadata.getMethod()).code())
                   .end(replyMsg.getData().encodePrettily());
            } else {
                ctx.response()
                   .setStatusCode(HttpStatusMapping.error(metadata.getMethod(), replyMsg.getError().getCode()).code())
                   .end(replyMsg.getError().toJson().encodePrettily());
            }
        } else {
            ctx.response().setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end();
            logger.error("No reply from cluster receiver. Address: {} - Action: {}", metadata.getAddress(),
                         metadata.getAction());
        }
    }

    private void registerNotFoundHandler(RoutingContext routingContext) {
        String uri = routingContext.request().absoluteURI();
        routingContext.response()
                      .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                      .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                      .end(new JsonObject().put("uri", uri).put("message", "Resource Not Found").encodePrettily());
    }

    private void registerIndexHandler(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(new JsonObject().put("name", "edge-connector-rest")
                                     .put("version", "1.0.0-SNAPSHOT")
                                     .put("vert.x_version", "3.4.1")
                                     .put("java_version", "8.0")
                                     .encodePrettily());
    }

}
