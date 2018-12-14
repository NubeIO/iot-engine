package com.nubeiot.core.http;

import com.nubeiot.core.component.IComponentProvider;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface IHttpServerProvider extends IComponentProvider {

    static HttpServer create(Vertx vertx, JsonObject rootCfg, HttpServerRouter httpRouter) {
        JsonObject httpCfg = IComponentProvider.computeConfig("httpServer.json", HttpServer.HTTP_CFG_NAME, rootCfg);
        return new HttpServer(vertx, httpCfg, httpRouter);
    }

}
