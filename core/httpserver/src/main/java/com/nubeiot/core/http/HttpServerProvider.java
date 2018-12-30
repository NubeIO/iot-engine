package com.nubeiot.core.http;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.component.IComponentProvider;

import io.vertx.reactivex.core.Vertx;

public interface HttpServerProvider extends IComponentProvider {

    static HttpServer create(Vertx vertx, NubeConfig nubeConfig, HttpServerRouter httpRouter) {
        HttpConfig httpCfg = IComponentProvider.computeConfig("httpServer.json", HttpConfig.class, nubeConfig);
        return new HttpServer(vertx.getDelegate(), httpCfg, httpRouter);
    }

}
