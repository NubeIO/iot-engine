package com.nubeiot.core.http;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.StaticHandler;

import com.nubeiot.core.component.EventControllerBridge;
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
import com.nubeiot.core.http.HttpConfig.RestConfig.DynamicRouteConfig;
import com.nubeiot.core.http.HttpConfig.StaticWebConfig;
import com.nubeiot.core.http.HttpConfig.WebsocketConfig;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.handler.DownloadFileHandler;
import com.nubeiot.core.http.handler.FailureContextHandler;
import com.nubeiot.core.http.handler.NotFoundContextHandler;
import com.nubeiot.core.http.handler.RestEventResponseHandler;
import com.nubeiot.core.http.handler.UploadFileHandler;
import com.nubeiot.core.http.handler.UploadListener;
import com.nubeiot.core.http.handler.WebsocketBridgeEventHandler;
import com.nubeiot.core.http.rest.RestApisBuilder;
import com.nubeiot.core.http.ws.WebsocketEventBuilder;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public final class HttpServer extends UnitVerticle<HttpConfig, HttpServerContext> {

    public final static String SERVER_INFO_DATA_KEY = "SERVER_INFO";
    private static final int MB = 1024 * 1024;
    @NonNull
    private final HttpServerRouter httpRouter;
    private io.vertx.core.http.HttpServer httpServer;
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
                SharedDataDelegate.addLocalDataValue(vertx, getSharedKey(), SERVER_INFO_DATA_KEY, info);
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

    private ServerInfo createServerInfo(Router handler, int port) {
        final RestConfig restCfg = config.getRestConfig();
        final DynamicRouteConfig dynamicCfg = restCfg.getDynamicConfig();
        final WebsocketConfig wsCfg = config.getWebsocketConfig();
        final FileStorageConfig storageCfg = config.getFileStorageConfig();
        final DownloadConfig downCfg = storageCfg.getDownloadConfig();
        final UploadConfig uploadCfg = storageCfg.getUploadConfig();
        return ServerInfo.siBuilder()
                         .host(config.getHost())
                         .port(port)
                         .publicHost(config.publicServerUrl())
                         .apiPath(restCfg.isEnabled() ? restCfg.getRootApi() : null)
                         .wsPath(wsCfg.isEnabled() ? wsCfg.getRootWs() : null)
                         .servicePath(restCfg.isEnabled() && dynamicCfg.isEnabled() ? dynamicCfg.getPath() : null)
                         .downloadPath(storageCfg.isEnabled() && downCfg.isEnabled() ? downCfg.getPath() : null)
                         .uploadPath(storageCfg.isEnabled() && uploadCfg.isEnabled() ? uploadCfg.getPath() : null)
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
                      .handler(corsHandler)
                      .handler(ResponseContentTypeHandler.create())
                      .handler(ResponseTimeHandler.create())
                      .failureHandler(ResponseTimeHandler.create())
                      .failureHandler(new FailureContextHandler());
            String pathNoUpload = "(?!" + config.getFileStorageConfig().getUploadConfig().getPath() + ").+";
            initFileStorageRouter(mainRouter, config.getFileStorageConfig(), config.publicServerUrl());
            mainRouter.routeWithRegex(pathNoUpload)
                      .handler(BodyHandler.create(false).setBodyLimit(config.getMaxBodySizeMB() * MB));
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
            final StaticHandler staticHandler = StaticHandler.create();
            if (webConfig.isInResource()) {
                staticHandler.setWebRoot(webConfig.getWebRoot());
            } else {
                String webDir = FileUtils.createFolder(dataDir, webConfig.getWebRoot());
                logger.info("Static web dir {}", webDir);
                staticHandler.setEnableRangeSupport(true)
                             .setSendVaryHeader(true)
                             .setFilesReadOnly(false)
                             .setAllowRootFileSystemAccess(true)
                             .setIncludeHidden(false)
                             .setWebRoot(webDir);
            }
            mainRouter.route(Urls.combinePath(webConfig.getWebPath(), ApiConstants.WILDCARDS_ANY_PATH))
                      .handler(staticHandler);
        }
    }

    private Router initRestRouter(Router router, RestConfig restConfig) {
        if (!restConfig.isEnabled()) {
            return router;
        }
        this.dataDir = this.getSharedData(SharedDataDelegate.SHARED_DATADIR, FileUtils.DEFAULT_DATADIR.toString());
        return new RestApisBuilder(vertx, router).rootApi(restConfig.getRootApi())
                                                 .registerApi(httpRouter.getRestApiClass())
                                                 .registerEventBusApi(httpRouter.getRestEventApiClass())
                                                 .dynamicRouteConfig(restConfig.getDynamicConfig())
                                                 .addEventController(
                                                     this.getSharedData(SharedDataDelegate.SHARED_EVENTBUS,
                                                                        EventControllerBridge.getInstance()
                                                                                             .getEventController(
                                                                                                 vertx)))
                                                 .build();
    }

    private Router initFileStorageRouter(Router router, FileStorageConfig storageConfig, String publicUrl) {
        if (!storageConfig.isEnabled()) {
            return router;
        }
        Path storageDir = Paths.get(FileUtils.createFolder(dataDir, storageConfig.getDir()));

        //FIXME: Need to address in backend and frontend in parallel
        if (storageConfig.isExternalHandler()) {
            router.route()
                  .handler(BodyHandler.create(storageDir.toString())
                                      .setBodyLimit(storageConfig.getUploadConfig().getMaxBodySizeMB() * MB));
            router.route()
                  .handler(StaticHandler.create()
                                        .setEnableRangeSupport(true)
                                        .setSendVaryHeader(true)
                                        .setFilesReadOnly(true)
                                        .setAllowRootFileSystemAccess(true)
                                        .setIncludeHidden(false)
                                        .setWebRoot(storageDir.toString()));
            router.get(Urls.combinePath(storageConfig.getDownloadConfig().getPath(), ApiConstants.WILDCARDS_ANY_PATH));
            return router;
        }
        initUploadRouter(router, storageDir, storageConfig.getUploadConfig(), publicUrl);
        initDownloadRouter(router, storageDir, storageConfig.getDownloadConfig());
        return router;
    }

    private Router initUploadRouter(Router router, Path storageDir, UploadConfig uploadCfg, String publicUrl) {
        if (!uploadCfg.isEnabled()) {
            return router;
        }
        logger.info("Init Upload router: '{}'...", uploadCfg.getPath());
        EventController controller = this.getSharedData(SharedDataDelegate.SHARED_EVENTBUS,
                                                        EventControllerBridge.getInstance().getEventController(vertx));
        EventModel listenerEvent = EventModel.builder()
                                             .address(Strings.fallback(uploadCfg.getListenerAddress(),
                                                                       getSharedKey() + ".upload"))
                                             .event(EventAction.CREATE)
                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                             .local(true)
                                             .build();
        String handlerClass = uploadCfg.getHandlerClass();
        String listenerClass = uploadCfg.getListenerClass();
        controller.register(listenerEvent,
                            UploadListener.create(listenerClass, new ArrayList<>(listenerEvent.getEvents())));
        router.post(uploadCfg.getPath())
              .handler(BodyHandler.create(storageDir.toString()).setBodyLimit(uploadCfg.getMaxBodySizeMB() * MB))
              .handler(UploadFileHandler.create(handlerClass, controller, listenerEvent, storageDir, publicUrl))
              .handler(new RestEventResponseHandler())
              .produces(HttpUtils.DEFAULT_CONTENT_TYPE);
        return router;
    }

    private Router initDownloadRouter(Router router, Path storageDir, DownloadConfig downloadCfg) {
        if (!downloadCfg.isEnabled()) {
            return router;
        }
        logger.info("Init Download router: '{}'...", downloadCfg.getPath());
        router.get(Urls.combinePath(downloadCfg.getPath(), ApiConstants.WILDCARDS_ANY_PATH))
              .handler(StaticHandler.create()
                                    .setEnableRangeSupport(true)
                                    .setSendVaryHeader(true)
                                    .setFilesReadOnly(false)
                                    .setAllowRootFileSystemAccess(true)
                                    .setIncludeHidden(false)
                                    .setWebRoot(storageDir.toString()))
              .handler(DownloadFileHandler.create(downloadCfg.getHandlerClass(), downloadCfg.getPath(), storageDir));
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
