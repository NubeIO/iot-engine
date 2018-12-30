package com.nubeiot.core.micro;

import com.nubeiot.core.component.IComponentProvider;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface IMicroProvider extends IComponentProvider {

    static Microservice create(Vertx vertx, JsonObject rootCfg) {
        MicroConfig microCfg = IComponentProvider.computeConfig("micro.json", MicroConfig.class, rootCfg);
        return new Microservice(vertx, microCfg);
    }

}
