package io.nubespark;

import io.nubespark.controller.RulesController;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.reactivex.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

/**
 * Created by topsykretts on 4/26/18.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SqlEngineRestVerticle extends RxMicroServiceVerticle {

    private RulesController controller;
    private Logger logger = LoggerFactory.getLogger(SqlEngineRestVerticle.class);

    @Override
    public void start() {
        super.start();
        logger.info("Config on sql engine rest app is:\n");
        logger.debug(Json.encodePrettily(config()));
        startWebApp().subscribe(
                httpServer -> logger.info("Web server started at " + httpServer.actualPort()),
                throwable -> logger.error("Cannot start server: " + throwable.getLocalizedMessage())
        );

        publishHttpEndpoint("io.nubespark.sql.engine", "0.0.0.0", config().getInteger("http.port", 8080)).subscribe(
                ignored -> {
                },
                throwable -> logger.error("Cannot publish: " + throwable.getLocalizedMessage())
        );


        controller = new RulesController(vertx.getDelegate());
    }

    private Single<HttpServer> startWebApp() {
        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message.
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();

            response.putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new JsonObject()
                            .put("name", "sql-engine-rest")
                            .put("version", "1.0")
                            .put("vert.x_version", "3.4.1")
                            .put("java_version", "8.0")
                    ));
        });

        router.route("/*").handler(BodyHandler.create());
        router.get("/tag/:id").handler(routingContext -> controller.getOne(routingContext.getDelegate()));
        router.post("/engine").handler(routingContext -> controller.getFiloData(routingContext.getDelegate()));

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
        return vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", 8080)
                );
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
