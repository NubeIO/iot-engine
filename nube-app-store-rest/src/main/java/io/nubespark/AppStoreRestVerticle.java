package io.nubespark;

import static io.nubespark.constants.Port.APP_STORE_PORT;

import java.util.stream.Collectors;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.events.Event;
import io.nubespark.events.EventMessage;
import io.nubespark.exceptions.HttpStatusMapping;
import io.nubespark.utils.Runner;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.RxRestAPIVerticle;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
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

/**
 * Created by topsykretts on 4/28/18.
 */
public class AppStoreRestVerticle extends RxRestAPIVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AppStoreRestVerticle.class);

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-app-store-rest/src/main/java/";
        Runner.runExample(JAVA_DIR, AppStoreRestVerticle.class);
    }

    @Override
    public void start(io.vertx.core.Future<Void> future) {
        super.start();
        logger.info("Config on app store REST {}", config());
        startWebApp().flatMap(httpServer -> publishHttp()).subscribe(ignored -> future.complete(), future::fail);
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint("io.nubespark.sql-hive.engine", "0.0.0.0",
                                   config().getInteger("http.port", APP_STORE_PORT)).doOnError(
                throwable -> logger.error("Cannot publish endpoint", throwable));
    }

    private Single<HttpServer> startWebApp() {
        // Create a router object.
        Router router = Router.router(vertx);

        // creating body handler
        router.route("/").handler(this::indexHandler);
        router.route().handler(BodyHandler.create());
        Event.CONTROL_MODULE.getEventMap()
                            .values()
                            .parallelStream()
                            .forEach(metadata -> router.route(metadata.getMethod(), metadata.getEndpoint())
                                                       .handler(ctx -> registerModuleControl(ctx, metadata)));
        router.get("/nodes").handler(this::getNodes);
        // This is last handler that gives not found message
        router.route().last().handler(this::handlePageNotFound);

        // Create the HTTP server and pass the "accept" method to the request handler.
        Single<HttpServer> server = createHttpServer(router, config().getString("http.host", "0.0.0.0"),
                                                     config().getInteger("http.port", APP_STORE_PORT));
        return server.doOnSuccess(httpServer -> logger.info("Web Server started at {}", httpServer.actualPort()))
                     .doOnError(throwable -> logger.error("Cannot start server", throwable));
    }

    private void getNodes(RoutingContext ctx) {
        JsonArray nodesInfo = Hazelcast.getAllHazelcastInstances()
                                       .parallelStream()
                                       .map(this::getNodeInfo)
                                       .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        ctx.response()
           .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
           .putHeader("Access-Control-Allow-Origin", "*")
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

    private void registerModuleControl(RoutingContext ctx, Event.Metadata metadata) {
        EventMessage msg = EventMessage.success(metadata.getAction(), ctx.getBodyAsJson());
        logger.info("Receive message from endpoint: {}", msg.toJson().encode());
        getVertx().eventBus()
                  .send(Event.CONTROL_MODULE.getAddress(), msg.toJson(), reply -> handleReply(ctx, metadata, reply));
    }

    private void handleReply(RoutingContext ctx, Event.Metadata metadata, AsyncResult<Message<Object>> reply) {
        if (reply.succeeded()) {
            EventMessage replyMsg = EventMessage.from(reply.result().body());
            logger.info("Receive message from backend: {}", replyMsg.toJson().encode());
            ctx.response().putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON);
            if (replyMsg.isOk()) {
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
            logger.error("No reply from cluster receiver. Address: {} - Action: {}", Event.CONTROL_MODULE.getAddress(),
                         metadata.getAction());
        }
    }

    private void handlePageNotFound(RoutingContext routingContext) {
        String uri = routingContext.request().absoluteURI();
        routingContext.response()
                      .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                      .setStatusCode(404)
                      .end(Json.encodePrettily(new JsonObject().put("uri", uri)
                                                               .put("status", 404)
                                                               .put("message", "Resource Not Found")));
    }

    // Returns verticle properties in json
    private void indexHandler(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(new JsonObject().put("name", "app-store-rest")
                                                         .put("version", "1.0")
                                                         .put("vert.x_version", "3.4.1")
                                                         .put("java_version", "8.0")));
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

}
