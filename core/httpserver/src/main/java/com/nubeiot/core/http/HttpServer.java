package com.nubeiot.core.http;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.StaticHandler;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.core.http.HttpConfig.CorsOptions;
import com.nubeiot.core.http.HttpConfig.FileStorageConfig;
import com.nubeiot.core.http.HttpConfig.FileStorageConfig.DownloadConfig;
import com.nubeiot.core.http.HttpConfig.FileStorageConfig.UploadConfig;
import com.nubeiot.core.http.HttpConfig.RestConfig;
import com.nubeiot.core.http.HttpConfig.StaticWebConfig;
import com.nubeiot.core.http.HttpConfig.WebsocketConfig;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.handler.DownloadFileHandler;
import com.nubeiot.core.http.handler.FailureContextHandler;
import com.nubeiot.core.http.handler.NotFoundContextHandler;
import com.nubeiot.core.http.handler.UploadFileHandler;
import com.nubeiot.core.http.handler.WebsocketBridgeEventHandler;
import com.nubeiot.core.http.rest.RestApisBuilder;
import com.nubeiot.core.http.ws.WebsocketEventBuilder;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public final class HttpServer extends UnitVerticle<HttpConfig, HttpServerContext> {

    public final static String SERVER_INFO_DATA_KEY = "SERVER_INFO";

    @NonNull
    private final HttpServerRouter httpRouter;
    private io.vertx.core.http.HttpServer httpServer;
    private EventModel uploadListenerEvent;
    private String dataDir;

    HttpServer(HttpServerRouter httpRouter) {
        super(new HttpServerContext());
        this.httpRouter = httpRouter;
    }

    @Override
    public void start(Future<Void> future) {
        logger.info("Starting HTTP Server...");
        super.start();
        this.dataDir = this.getSharedData(SharedDataDelegate.SHARED_DATADIR, FileUtils.DEFAULT_DATADIR.toString());
        HttpServerOptions options = new HttpServerOptions(config.getOptions()).setHost(config.getHost())
                                                                              .setPort(config.getPort());
        final Router handler = initRouter();
        this.httpServer = vertx.createHttpServer(options).requestHandler(handler).listen(event -> {
            if (event.succeeded()) {
                int port = event.result().actualPort();
                logger.info("Web Server started at {}", port);
                ServerInfo info = createServerInfo(handler, port);
                this.vertx.sharedData().getLocalMap(this.getSharedKey()).put(SERVER_INFO_DATA_KEY, info);
                this.getContext().create(info, uploadListenerEvent);
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

    private ServerInfo createServerInfo(Router handler, int port) {
        return ServerInfo.builder()
                         .host(config.getHost())
                         .port(port)
                         .apiPath(config.getRestConfig().isEnabled() ? config.getRestConfig().getRootApi() : null)
                         .wsPath(
                             config.getWebsocketConfig().isEnabled() ? config.getWebsocketConfig().getRootWs() : null)
                         .servicePath(
                             config.getRestConfig().isEnabled() && config.getRestConfig().getDynamicConfig().isEnabled()
                             ? config.getRestConfig().getDynamicConfig().getPath()
                             : null)
                         .downloadPath(config.getFileStorageConfig().isEnabled() ? config.getFileStorageConfig()
                                                                                         .getDownloadConfig()
                                                                                         .getPath() : null)
                         .uploadPath(config.getFileStorageConfig().isEnabled() ? config.getFileStorageConfig()
                                                                                       .getUploadConfig()
                                                                                       .getPath() : null)
                         .router(handler)
                         .build();
    }

    @Override
    public Class<HttpConfig> configClass() { return HttpConfig.class; }

    @Override
    public String configFile() { return "httpServer.json"; }

    private Router initRouter() {
        try {
            Router mainRouter = Router.router(vertx);
            CorsOptions corsOptions = config.getCorsOptions();
            CorsHandler corsHandler = CorsHandler.create(corsOptions.getAllowedOriginPattern())
                                                 .allowedMethods(corsOptions.getAllowedMethods())
                                                 .allowedHeaders(corsOptions.getAllowedHeaders())
                                                 .allowCredentials(corsOptions.isAllowCredentials())
                                                 .exposedHeaders(corsOptions.getExposedHeaders())
                                                 .maxAgeSeconds(corsOptions.getMaxAgeSeconds());
            mainRouter.route()
                      .handler(BodyHandler.create())
                      .handler(corsHandler)
                      .handler(ResponseContentTypeHandler.create())
                      .handler(ResponseTimeHandler.create())
                      .failureHandler(ResponseTimeHandler.create())
                      .failureHandler(new FailureContextHandler());
            initFileStorageRouter(mainRouter, config.getFileStorageConfig());
            initWebSocketRouter(mainRouter, config.getWebsocketConfig());
            initHttp2Router(mainRouter);
            initRestRouter(mainRouter, config.getRestConfig());
            initStaticWebRouter(mainRouter, config.getStaticWebConfig());
            mainRouter.route().last().handler(new NotFoundContextHandler());
            return mainRouter;
        } catch (NubeException e) {
            throw new InitializerError("Error when initializing http server route", e);
        }
    }

    private void initStaticWebRouter(Router mainRouter, StaticWebConfig webConfig) {
        if (webConfig.isEnabled()) {
            String webDir = FileUtils.createFolder(dataDir, webConfig.getWebRoot());
            mainRouter.route(Urls.combinePath(ApiConstants.SAMPLE_PATH, ApiConstants.WILDCARDS_ANY_PATH))
                      .handler(StaticHandler.create(webDir));
        }
    }

    private Router initRestRouter(Router router, RestConfig restConfig) {
        if (!restConfig.isEnabled()) {
            return router;
        }
        return new RestApisBuilder(vertx, router).rootApi(restConfig.getRootApi())
                                                 .registerApi(httpRouter.getRestApiClass())
                                                 .registerEventBusApi(httpRouter.getRestEventApiClass())
                                                 .dynamicRouteConfig(restConfig.getDynamicConfig())
                                                 .build();
    }

    private Router initFileStorageRouter(Router router, FileStorageConfig storageConfig) {
        if (!storageConfig.isEnabled()) {
            return router;
        }
        Path storageDir = Paths.get(FileUtils.createFolder(dataDir, storageConfig.getDir()));
        initUploadRouter(router, storageDir, storageConfig.getUploadConfig());
        initDownloadRouter(router, storageDir, storageConfig.getDownloadConfig());
        return router;
    }

    private Router initUploadRouter(Router router, Path storageDir, UploadConfig uploadCfg) {
        if (!uploadCfg.isEnabled()) {
            return router;
        }
        logger.info("Init Upload router...");
        EventController controller = this.getSharedData(SharedDataDelegate.SHARED_EVENTBUS, new EventController(vertx));
        this.uploadListenerEvent = EventModel.builder()
                                             .address(Strings.fallback(uploadCfg.getListenerAddress(), getSharedKey()))
                                             .event(EventAction.CREATE)
                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                             .local(true)
                                             .build();
        router.post(uploadCfg.getPath())
              .handler(BodyHandler.create(storageDir.toString())
                                  .setHandleFileUploads(true)
                                  .setBodyLimit(uploadCfg.getMaxSize() * 1024))
              .handler(
                  UploadFileHandler.create(uploadCfg.getHandlerClass(), controller, uploadListenerEvent, storageDir,
                                           config.publicUrl()));
        return router;
    }

    private Router initDownloadRouter(Router router, Path storageDir, DownloadConfig downloadConfig) {
        if (!downloadConfig.isEnabled()) {
            return router;
        }
        logger.info("Init Download router...");
        router.get(Urls.combinePath(downloadConfig.getPath(), ApiConstants.WILDCARDS_ANY_PATH))
              .handler(StaticHandler.create()
                                    .setEnableRangeSupport(true)
                                    .setSendVaryHeader(true)
                                    .setFilesReadOnly(false)
                                    .setAllowRootFileSystemAccess(true)
                                    .setIncludeHidden(false)
                                    .setWebRoot(storageDir.toString()))
              .handler(DownloadFileHandler.create(downloadConfig.getPath(), storageDir));
        return router;
    }

    private Router initWebSocketRouter(Router router, WebsocketConfig websocketCfg) {
        if (!websocketCfg.isEnabled()) {
            return router;
        }
        logger.info("Init Websocket router...");
        return new WebsocketEventBuilder(vertx, router).rootWs(websocketCfg.getRootWs())
                                                       .register(httpRouter.getWebsocketEvents())
                                                       .handler(WebsocketBridgeEventHandler.class)
                                                       .options(websocketCfg)
                                                       .build();
    }

    private Router initHttp2Router(Router router) { return router; }

}
