package io.nubespark;

import io.nubespark.controller.ErrorCodeException;
import io.nubespark.controller.RulesController;
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

import static io.nubespark.controller.ErrorCodes.NO_QUERY_SPECIFIED;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

/**
 * Created by topsykretts on 4/26/18.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SqlEngineRestVerticle extends RxMicroServiceVerticle {

    private RulesController controller;
    private Logger logger = LoggerFactory.getLogger(SqlEngineRestVerticle.class);

    @Override
    public void start(Future future) {
        super.start();
        logger.info("Config on sql engine rest app is:\n");
        logger.debug(Json.encodePrettily(config()));

        final Single<HttpServer> startWebSingle = startWebApp()
                .doOnError(throwable -> logger.error("Cannot start server: " + throwable.getLocalizedMessage()));

        final Single<Record> publishHttp = publishHttpEndpoint("io.nubespark.sql.engine", "0.0.0.0", config().getInteger("http.port", 8080))
                .doOnError(throwable -> logger.error("Cannot publish: " + throwable.getLocalizedMessage()));

        Single.zip(startWebSingle,
                publishHttp,
                (httpServer, record) -> {
                    logger.info("Web server started at " + httpServer.actualPort());
                    return record;
                }).subscribe(ignored -> future.complete(), future::fail);


        controller = new RulesController(vertx);
    }

    private Single<HttpServer> startWebApp() {
        // Create a router object.
        Router router = Router.router(vertx);

        router.route("/").handler(this::indexHandler);

        router.route("/*").handler(BodyHandler.create());
        router.get("/tag/:id").handler(this::tagGetHandler);
        router.post("/engine").handler(this::enginePostHandler);

        // This is last handler that gives not found message
        router.route().last().handler(this::handlePageNotFound);

        // Create the HTTP server and pass the "accept" method to the request handler.
        return vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", 8080)
                );
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

    private void enginePostHandler(RoutingContext routingContext) {
        // Check if we have a query in body
        JsonObject body = routingContext.getBodyAsJson();
        String query = null;
        if (body != null) {
            query = body.getString("query", null);
        }
        if (query == null) {
            // Return query not specified error
            handleError(new ErrorCodeException(NO_QUERY_SPECIFIED), routingContext);
        } else {
            controller.getFiloData(query).subscribe(
                    replyJson -> routingContext.response()
                            .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(replyJson)),
                    throwable -> handleError(throwable, routingContext));
        }

    }

    private void handleError(Throwable throwable, RoutingContext routingContext) {
        if (throwable instanceof ErrorCodeException) {
            switch (((ErrorCodeException) throwable).getErrorCodes()) {
                case BAD_ACTION:
                    routingContext.response()
                            .setStatusCode(403)
                            .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(new JsonObject().put("message", "You do not have permission to run this query.")));
                    break;
                case NO_QUERY_SPECIFIED:
                    routingContext.response()
                            .setStatusCode(400)
                            .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(new JsonObject().put("message", "Request must have a valid JSON body with 'query' field.")));
                    break;
            }
        } else {
            routingContext.response()
                    .setStatusCode(500)
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .end(Json.encodePrettily(new JsonObject().put("message", "Server Error" + throwable.getMessage())));
        }
    }


    private void tagGetHandler(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String id = request.getParam("id");
        controller.getOne(id).subscribe(
                json -> routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encodePrettily(json)),
                throwable -> handleError(throwable, routingContext));
    }

    // Returns verticle properties in json
    private void indexHandler(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        response.putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(new JsonObject()
                        .put("name", "sql-engine-rest")
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
