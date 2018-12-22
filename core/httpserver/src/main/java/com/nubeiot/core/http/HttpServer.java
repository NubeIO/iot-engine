package com.nubeiot.core.http;

import java.util.Objects;
import javax.ws.rs.core.MediaType;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.handler.ApiExceptionHandler;
import com.nubeiot.core.http.handler.ApiJsonWriter;
import com.nubeiot.core.http.handler.FailureContextHandler;
import com.nubeiot.core.http.handler.NotFoundContextHandler;
import com.nubeiot.core.http.handler.RestEventResponseHandler;
import com.nubeiot.core.http.utils.Urls;
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
        createHttpServer(initRouter(), host, port).subscribe(
                httpServer -> logger.info("Web Server started at {}", httpServer.actualPort()),
                throwable -> logger.error("Cannot start server", throwable));
    }

    @Override
    public void stop() throws NubeException {
        if (Objects.nonNull(this.httpServer)) {
            this.httpServer.close();
        }
    }

    private Single<io.vertx.reactivex.core.http.HttpServer> createHttpServer(Router router, String host, int port) {
        this.httpServer = vertx.createHttpServer();
        return httpServer.requestHandler(router).rxListen(port, host);
    }

    private Router initRouter() {
        Router router = Router.router(vertx);
        initUploadRouter(router);
        initDownloadRouter(router);
        initWebSocketRouter(router);
        initHttp2Router(router);
        initRestRouter(router);
        //TODO Cors Handler from config
        CorsHandler corsHandler = CorsHandler.create("*").allowedMethods(ApiConstants.DEFAULT_CORS_HTTP_METHOD);
        router.route()
              .handler(io.vertx.reactivex.ext.web.handler.CorsHandler.newInstance(corsHandler))
              .handler(ResponseContentTypeHandler.create())
              .handler(ResponseTimeHandler.create())
              .failureHandler(ResponseTimeHandler.create())
              .failureHandler(new FailureContextHandler());
        router.route().last().handler(new NotFoundContextHandler());
        return router;
    }

    private Router initRestRouter(Router router) {
        boolean enabled = httpConfig.getBoolean("enabled", true);
        if (!enabled) {
            return router;
        }
        if (!httpRouter.hasRestApi()) {
            throw new InitializerError("No REST API given, register at least one.");
        }
        String rootApi = httpConfig.getString("rootApi", ApiConstants.ROOT_API_PATH);
        String wildCards = Urls.combinePath(rootApi, ApiConstants.PATH_WILDCARDS);
        initRestApiRouter(initEventBusApiRouter(router, rootApi)).route(wildCards)
                                                                 .handler(new RestEventResponseHandler())
                                                                 .produces(ApiConstants.DEFAULT_CONTENT_TYPE);
        return router;
    }

    private Router initRestApiRouter(Router router) {
        if (!httpRouter.hasApi()) {
            return router;
        }
        Class[] classes = httpRouter.getRestApiClass().toArray(new Class[] {});
        return Router.newInstance(new RestBuilder(router.getDelegate()).errorHandler(ApiExceptionHandler.class)
                                                                       .writer(MediaType.APPLICATION_JSON_TYPE,
                                                                               ApiJsonWriter.class)
                                                                       .register((Object[]) classes)
                                                                       .build());
    }

    private Router initEventBusApiRouter(Router router, String rootApi) {
        if (!httpRouter.hasEventBusApi()) {
            return router;
        }
        return new RestEventBuilder(router).rootApi(rootApi).register(httpRouter.getRestEventApiClass()).build();
    }

    private Router initUploadRouter(Router router) {
        return router;
    }

    private Router initDownloadRouter(Router router) {
        return router;
    }

    private Router initWebSocketRouter(Router router) {
        final JsonObject socketCfg = httpConfig.getJsonObject(SOCKET_CFG_NAME, new JsonObject());
        boolean enabled = socketCfg.getBoolean("enabled", false);
        String rootWs = httpConfig.getString("rootWS", ApiConstants.ROOT_WS_PATH);
        if (enabled) {
            return new WebsocketEventBuilder(vertx, router).rootWs(rootWs)
                                                           .register(httpRouter.getWebsocketEvents())
                                                           .build();
        }
        return router;
    }

    private Router initHttp2Router(Router router) {
        return router;
    }

}
