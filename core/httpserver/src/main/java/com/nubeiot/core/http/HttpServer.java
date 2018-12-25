package com.nubeiot.core.http;

import java.util.Objects;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.handler.FailureContextHandler;
import com.nubeiot.core.http.handler.NotFoundContextHandler;
import com.nubeiot.core.http.handler.WebsocketBridgeEventHandler;
import com.nubeiot.core.http.rest.RestApiBuilder;
import com.nubeiot.core.http.ws.WebsocketEventBuilder;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class HttpServer implements IComponent {

    private final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private final Vertx vertx;
    private final HttpConfig httpConfig;
    private final HttpServerRouter httpRouter;
    private io.vertx.core.http.HttpServer httpServer;

    @Override
    public void start() throws NubeException {
        logger.info("HTTP Server configuration: {}", httpConfig.toJson().encode());
        HttpServerOptions options = new HttpServerOptions(httpConfig.getOptions()).setHost(httpConfig.getHost())
                                                                                  .setPort(httpConfig.getPort());
        this.httpServer = vertx.createHttpServer(options).requestHandler(initRouter());
        io.vertx.reactivex.core.http.HttpServer.newInstance(httpServer)
                                               .rxListen()
                                               .subscribe(s -> logger.info("Web Server started at {}", s.actualPort()),
                                                          t -> logger.error("Cannot start server", t));
    }

    @Override
    public void stop() throws NubeException {
        if (Objects.nonNull(this.httpServer)) {
            this.httpServer.close();
        }
    }

    private Router initRouter() {
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);
        HttpConfig.CorsOptions corsOptions = httpConfig.getCorsOptions();
        CorsHandler corsHandler = CorsHandler.create(corsOptions.getAllowedOriginPattern())
                                             .allowedMethods(corsOptions.getAllowedMethods())
                                             .allowedHeaders(corsOptions.getAllowedHeaders())
                                             .allowCredentials(corsOptions.isAllowCredentials())
                                             .exposedHeaders(corsOptions.getExposedHeaders())
                                             .maxAgeSeconds(corsOptions.getMaxAgeSeconds());
        router.route().handler(BodyHandler.create()).handler(corsHandler)
              .handler(ResponseContentTypeHandler.create())
              .handler(ResponseTimeHandler.create())
              .failureHandler(ResponseTimeHandler.create())
              .failureHandler(new FailureContextHandler());
        initUploadRouter(router);
        initDownloadRouter(router);
        initWebSocketRouter(router);
        initHttp2Router(router);
        initRestRouter(router);
        router.route().last().handler(new NotFoundContextHandler());
        return router;
    }

    private Router initRestRouter(Router router) {
        if (!httpConfig.isEnabled()) {
            return router;
        }
        return new RestApiBuilder(router).rootApi(httpConfig.getRootApi())
                                         .registerApi(httpRouter.getRestApiClass())
                                         .registerEventBusApi(httpRouter.getRestEventApiClass())
                                         .build();
    }

    private Router initUploadRouter(Router router) {
        return router;
    }

    private Router initDownloadRouter(Router router) {
        return router;
    }

    private Router initWebSocketRouter(Router router) {
        if (!httpConfig.getWebsocketCfg().isEnabled()) {
            return router;
        }
        return new WebsocketEventBuilder(vertx, router).rootWs(httpConfig.getWebsocketCfg().getRootWs())
                                                       .register(httpRouter.getWebsocketEvents())
                                                       .handler(WebsocketBridgeEventHandler.class)
                                                       .options(httpConfig.getWebsocketCfg())
                                                       .build();
    }

    private Router initHttp2Router(Router router) {
        return router;
    }

}
