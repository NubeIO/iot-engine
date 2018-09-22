package io.nubespark.hive;

import io.nubespark.hive.controller.RulesController;
import io.nubespark.utils.ErrorCodeException;
import io.nubespark.utils.ErrorHandler;
import io.nubespark.utils.Runner;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.serviceproxy.ServiceBinder;

import java.net.URL;
import java.net.URLClassLoader;

import static io.nubespark.constants.Port.HIVE_SERVER_PORT;
import static io.nubespark.hive.HiveService.SERVICE_ADDRESS;
import static io.nubespark.hive.HiveService.SERVICE_NAME;
import static io.nubespark.utils.ErrorCodes.NO_QUERY_SPECIFIED;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

/**
 * Created by topsykretts on 4/26/18.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class HiveVerticle extends RxMicroServiceVerticle {

    private Logger logger = LoggerFactory.getLogger(HiveVerticle.class);
    private RulesController controller;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-vertx-hive/src/main/java/";
        Runner.runExample(JAVA_DIR, HiveVerticle.class);
    }

    @Override
    public void start(Future<Void> startFuture) {
        super.start();
        logServerDetails();

        startWebApp()
            .flatMap(httpServer -> publishHttp())
            .flatMap(ignored -> HiveService.create(vertx, config().getJsonObject("hiveConfig"))
                .doOnSuccess(hiveService -> {
                    ServiceBinder binder = new ServiceBinder(vertx.getDelegate());
                    binder.setAddress(SERVICE_ADDRESS).register(HiveService.class, hiveService);
                    logger.info("Service bound to " + binder);
                })).flatMap(ignored -> publishMessageSource(SERVICE_NAME, SERVICE_ADDRESS))
            .subscribe(record -> startFuture.complete(), startFuture::fail);

        controller = new RulesController(vertx);
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint("io.nubespark.sql-hive.engine", "0.0.0.0", config().getInteger("http.port", HIVE_SERVER_PORT))
            .doOnError(throwable -> logger.error("Cannot publish: " + throwable.getLocalizedMessage()));
    }

    private Single<HttpServer> startWebApp() {
        // Create a router object.
        Router router = Router.router(vertx);
        router.route("/").handler(this::indexHandler);
        router.route("/*").handler(BodyHandler.create());
        router.get("/tag/:id").handler(this::tagGetHandler);
        router.post("/engine").handler(this::engineHivePostHandler);

        // This is last handler that gives not found message
        router.route().last().handler(this::handlePageNotFound);

        // Create the HTTP server and pass the "accept" method to the request handler.
        return vertx.createHttpServer()
            .requestHandler(router::accept)
            .rxListen(config().getInteger("http.port", HIVE_SERVER_PORT))
            .doOnSuccess(httpServer -> logger.info("Web server started at " + httpServer.actualPort()))
            .doOnError(throwable -> logger.error("Cannot start server: " + throwable.getLocalizedMessage()));
    }


    private void tagGetHandler(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String id = request.getParam("id");
        controller.getOne(id).subscribe(
            json -> routingContext.response()
                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(json)),
            throwable -> ErrorHandler.handleError(throwable, routingContext));
    }

    private void engineHivePostHandler(RoutingContext routingContext) {
        // Check if we have a query in body
        JsonObject body = routingContext.getBodyAsJson();
        String query = null;
        if (body != null) {
            query = body.getString("query", null);
        }
        if (query == null) {
            // Return query not specified error
            ErrorHandler.handleError(new ErrorCodeException(NO_QUERY_SPECIFIED), routingContext);
        } else {
            controller.getFiloData(query).subscribe(
                replyJson -> routingContext.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .end(Json.encodePrettily(replyJson)),
                throwable -> ErrorHandler.handleError(throwable, routingContext));
        }

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
                .put("name", "hive-engine-rest")
                .put("version", "1.0")
                .put("vert.x_version", "3.4.1")
                .put("java_version", "8.0")
            ));
    }

    private void logServerDetails() {
        logger.info("Config on Hive Engine app");
        logger.info(Json.encodePrettily(config()));

        logger.info("Classpath of Hive Engine app = " + System.getProperty("java.class.path"));
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        for (URL url : urls) {
            logger.info(url.getFile());
        }
        logger.info("Current thread loader = " + Thread.currentThread().getContextClassLoader());
        logger.info(HiveVerticle.class.getClassLoader());
    }

    protected Logger getLogger() {
        return logger;
    }
}
