package com.nubeiot.core.http;

import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.StaticHandler;

import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.core.http.HttpConfig.CorsOptions;
import com.nubeiot.core.http.handler.FailureContextHandler;
import com.nubeiot.core.http.handler.NotFoundContextHandler;
import com.nubeiot.core.http.handler.WebsocketBridgeEventHandler;
import com.nubeiot.core.http.rest.RestApisBuilder;
import com.nubeiot.core.http.ws.WebsocketEventBuilder;

import lombok.NonNull;

public final class HttpServer extends UnitVerticle<HttpConfig, HttpServerContext> {

    public final static String SERVER_INFO_DATA_KEY = "SERVER_INFO";

    @NonNull
    private final HttpServerRouter httpRouter;
    private io.vertx.core.http.HttpServer httpServer;

    HttpServer(HttpServerRouter httpRouter) {
        super(new HttpServerContext());
        this.httpRouter = httpRouter;
    }

    @Override
    public void start(Future<Void> future) {
        logger.info("Starting HTTP Server...");
        super.start();
        HttpServerOptions options = new HttpServerOptions(config.getOptions()).setHost(config.getHost())
                                                                              .setPort(config.getPort());
        final Router handler = initRouter();
        this.httpServer = vertx.createHttpServer(options).requestHandler(handler).listen(event -> {
            if (event.succeeded()) {
                int port = event.result().actualPort();
                logger.info("Web Server started at {}", port);
                ServerInfo info = ServerInfo.builder()
                                            .host(config.getHost())
                                            .port(port)
                                            .apiPath(config.isEnabled() ? config.getRootApi() : null)
                                            .wsPath(config.getWebsocketCfg().isEnabled() ? config.getWebsocketCfg()
                                                                                                 .getRootWs() : null)
                                            .servicePath(config.getDynamicRouteConfig().isEnabled()
                                                         ? config.getDynamicRouteConfig().getPath()
                                                         : null)
                                            .downloadPath(ApiConstants.ROOT_DOWNLOAD_PATH)
                                            .uploadPath(ApiConstants.ROOT_UPLOAD_PATH)
                                            .router(handler)
                                            .build();
                this.vertx.sharedData().getLocalMap(this.getSharedKey()).put(SERVER_INFO_DATA_KEY, info);
                this.getContext().create(info);
                future.complete();
                return;
            }
            future.fail(NubeExceptionConverter.from(event.cause()));
        });
    }

    @Override
    public void stop() {
        if (Objects.nonNull(this.httpServer)) {
            this.httpServer.close();
        }
    }

    @Override
    public Class<HttpConfig> configClass() { return HttpConfig.class; }

    @Override
    public String configFile() { return "httpServer.json"; }

    private Router initRouter() {
        try {
            Router router = Router.router(vertx);
            CorsOptions corsOptions = config.getCorsOptions();
            CorsHandler corsHandler = CorsHandler.create(corsOptions.getAllowedOriginPattern())
                                                 .allowedMethods(corsOptions.getAllowedMethods())
                                                 .allowedHeaders(corsOptions.getAllowedHeaders())
                                                 .allowCredentials(corsOptions.isAllowCredentials())
                                                 .exposedHeaders(corsOptions.getExposedHeaders())
                                                 .maxAgeSeconds(corsOptions.getMaxAgeSeconds());
            router.route()
                  .handler(BodyHandler.create())
                  .handler(corsHandler)
                  .handler(ResponseContentTypeHandler.create())
                  .handler(ResponseTimeHandler.create())
                  .failureHandler(ResponseTimeHandler.create())
                  .failureHandler(new FailureContextHandler());
            initUploadRouter(router);
            initDownloadRouter(router);
            initWebSocketRouter(router);
            initHttp2Router(router);
            initRestRouter(router);
            if (config.isSample()) {
                router.route(ApiConstants.SAMPLE_PATH)
                      .handler(StaticHandler.create().setWebRoot(config.getSampleWebRoot()).setIncludeHidden(false));
            }
            router.route().last().handler(new NotFoundContextHandler());
            return router;
        } catch (NubeException e) {
            throw new InitializerError("Error when initializing http server route", e);
        }
    }

    private Router initRestRouter(Router router) {
        if (!config.isEnabled()) {
            return router;
        }
        return new RestApisBuilder(vertx, router).rootApi(config.getRootApi())
                                                 .registerApi(httpRouter.getRestApiClass())
                                                 .registerEventBusApi(httpRouter.getRestEventApiClass())
                                                 .dynamicRouteConfig(config.getDynamicRouteConfig())
                                                 .build();
    }

    private Router initUploadRouter(Router router) {
        return router;
    }

    private Router initDownloadRouter(Router router) { return router; }

    private Router initWebSocketRouter(Router router) {
        if (!config.getWebsocketCfg().isEnabled()) {
            return router;
        }
        return new WebsocketEventBuilder(vertx, router).rootWs(config.getWebsocketCfg().getRootWs())
                                                       .register(httpRouter.getWebsocketEvents())
                                                       .handler(WebsocketBridgeEventHandler.class)
                                                       .options(config.getWebsocketCfg())
                                                       .build();
    }

    private Router initHttp2Router(Router router) { return router; }

}
