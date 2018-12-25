package com.nubeiot.core.http;

import com.nubeiot.core.component.IComponentProvider;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface HttpServerProvider extends IComponentProvider {

    String HTTP_CFG_NAME = "__http__";

    static HttpServer create(Vertx vertx, JsonObject rootCfg, HttpServerRouter httpRouter) {
        JsonObject httpCfg = IComponentProvider.computeConfig("httpServer.json", HTTP_CFG_NAME, rootCfg);
        return new HttpServer(vertx.getDelegate(), httpCfg.mapTo(HttpConfig.class), httpRouter);
    }

}
