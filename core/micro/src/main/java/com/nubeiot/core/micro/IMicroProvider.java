package com.nubeiot.core.micro;

import com.nubeiot.core.component.IComponentProvider;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface IMicroProvider extends IComponentProvider {

    static Microservice create(Vertx vertx, JsonObject rootCfg) {
        JsonObject microCfg = IComponentProvider.computeConfig("micro.json", Microservice.MICRO_CFG_NAME, rootCfg);
        return new Microservice(vertx, microCfg);
    }

}
