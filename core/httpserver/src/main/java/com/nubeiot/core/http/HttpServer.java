package com.nubeiot.core.http;

import java.util.Objects;
import javax.ws.rs.core.MediaType;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.handler.ApiExceptionHandler;
import com.nubeiot.core.http.handler.ApiJsonWriter;
import com.nubeiot.core.http.handler.FailureContextHandler;
import com.nubeiot.core.http.handler.JsonContextHandler;
import com.nubeiot.core.http.handler.NotFoundContextHandler;
import com.zandero.rest.RestBuilder;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.reactivex.ext.web.handler.ResponseTimeHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class HttpServer implements IComponent {

    private final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    static final String HTTP_CFG_NAME = "__http__";
    static final String SOCKET_CFG_NAME = "__socket__";
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_SOCKET_PORT = 8080;
    private final Vertx vertx;
    private final JsonObject httpConfig;
    private final HttpServerRouter httpRouter;
    private io.vertx.reactivex.core.http.HttpServer httpServer;

    @Override
    public void start() throws NubeException {
        logger.info("HTTP Server configuration: {}", httpConfig.encode());
        String host = this.httpConfig.getString("host", DEFAULT_HOST);
        int port = this.httpConfig.getInteger("port", DEFAULT_PORT);
        createHttpServer(initRouter(), host, port).subscribe(httpServer -> {
            this.httpServer = httpServer;
            logger.info("Web Server started at {}", httpServer.actualPort());
        }, throwable -> logger.error("Cannot start server", throwable));
    }

    @Override
    public void stop() throws NubeException {
        if (Objects.nonNull(this.httpServer)) {
            this.httpServer.close();
        }
    }

    private Single<io.vertx.reactivex.core.http.HttpServer> createHttpServer(Router router, String host, int port) {
        return vertx.createHttpServer().requestHandler(router::accept).rxListen(port, host);
    }

    private Router initRouter() {
        if (!httpRouter.validate()) {
            throw new InitializerError("No REST API given, register at least one.");
        }
        Router router = Router.router(vertx);
        router = initUploadRouter(initDownloadRouter(router));
        router = initWebSocketRouter(router);
        router = initHttp2Router(router);
        router = initEventBusRouter(initRestApiRouter(router));
        CorsHandler corsHandler = CorsHandler.create("*").allowedMethods(ApiConstants.DEFAULT_CORS_HTTP_METHOD);
        router.route()
              .handler(io.vertx.reactivex.ext.web.handler.CorsHandler.newInstance(corsHandler))
              .handler(ResponseContentTypeHandler.create())
              .handler(ResponseTimeHandler.create())
              .failureHandler(ResponseTimeHandler.create())
              .failureHandler(new FailureContextHandler());
        router.route(httpConfig.getString("rootApi", ApiConstants.ROOT_API_PATH_WILDCARDS))
              .handler(new JsonContextHandler())
              .produces(ApiConstants.DEFAULT_CONTENT_TYPE);
        router.route().last().handler(new NotFoundContextHandler());
        return router;
    }

    private Router initRestApiRouter(Router router) {
        if (!httpRouter.hasApi()) {
            return router;
        }
        RestBuilder builder = new RestBuilder(router.getDelegate()).errorHandler(ApiExceptionHandler.class)
                                                                   .writer(MediaType.APPLICATION_JSON_TYPE,
                                                                           ApiJsonWriter.class);
        httpRouter.getRestApiClass().forEach(builder::register);
        return Router.newInstance(builder.build());
    }

    @SuppressWarnings("unchecked")
    private Router initEventBusRouter(Router router) {
        if (!httpRouter.hasEventBusApi()) {
            return router;
        }
        EventBusRestBuilder builder = new EventBusRestBuilder(router, vertx.eventBus());
        httpRouter.getEventBusRestApiClass().forEach(builder::register);
        return builder.build();
    }

    private Router initUploadRouter(Router router) {
        return router;
    }

    private Router initDownloadRouter(Router router) {
        return router;
    }

    private Router initWebSocketRouter(Router router) {
        return router;
    }

    private Router initHttp2Router(Router router) {
        return router;
    }

}
