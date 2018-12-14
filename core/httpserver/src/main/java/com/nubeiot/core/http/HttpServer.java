package com.nubeiot.core.http;

import javax.ws.rs.core.MediaType;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.handler.ApiExceptionHandler;
import com.nubeiot.core.http.handler.ApiJsonWriter;
import com.nubeiot.core.http.handler.ApiNotFoundWriter;
import com.nubeiot.core.http.handler.ContextFailureHandler;
import com.zandero.rest.RestBuilder;

import io.reactivex.Single;
import io.vertx.core.Future;
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

    static final String HTTP_CFG_NAME = "http";
    static final String SOCKET_CFG_NAME = "socket";
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_SOCKET_PORT = 8080;
    private final Vertx vertx;
    private final JsonObject httpConfig;
    private final HttpServerRouter httpRouter;

    @Override
    public void start(Future<Void> startFuture) throws NubeException {

    }

    @Override
    public void start() throws NubeException {
        String host = this.httpConfig.getString("host", DEFAULT_HOST);
        int port = this.httpConfig.getInteger("port", DEFAULT_PORT);
        createHttpServer(initRouter(), host, port).subscribe(
                httpServer -> logger.info("Web Server started at {}", httpServer.actualPort()),
                throwable -> logger.error("Cannot start server", throwable)).dispose();
    }

    private Router initRouter() {
        Router router = Router.router(vertx);
        CorsHandler corsHandler = CorsHandler.create("*").allowedMethods(ApiConstants.DEFAULT_CORS_HTTP_METHOD);
        router.route()
              .handler(io.vertx.reactivex.ext.web.handler.CorsHandler.newInstance(corsHandler))
              .handler(ResponseContentTypeHandler.create())
              .handler(ResponseTimeHandler.create())
              .failureHandler(new ContextFailureHandler());
        return initEventBusRouter(initRestApiRouter(router));
    }

    protected Router initRestApiRouter(Router router) {
        RestBuilder builder = new RestBuilder(router.getDelegate()).notFound(ApiNotFoundWriter.class)
                                                                   .errorHandler(ApiExceptionHandler.class)
                                                                   .writer(MediaType.APPLICATION_JSON_TYPE,
                                                                           ApiJsonWriter.class);
        httpRouter.getRestApiClass().forEach(builder::register);
        return Router.newInstance(builder.build());
    }

    @SuppressWarnings("unchecked")
    protected Router initEventBusRouter(Router router) {
        EventBusRestBuilder builder = new EventBusRestBuilder(router, vertx.eventBus());
        httpRouter.getEventBusRestApiClass().forEach(builder::register);
        return builder.build();
    }

    @Override
    public void stop() throws NubeException {

    }

    @Override
    public void stop(Future<Void> future) throws NubeException {

    }

    private Single<io.vertx.reactivex.core.http.HttpServer> createHttpServer(Router router, String host, int port) {
        return vertx.createHttpServer().requestHandler(router::accept).rxListen(port, host);
    }

}
