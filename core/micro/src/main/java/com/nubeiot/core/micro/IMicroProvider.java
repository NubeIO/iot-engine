package com.nubeiot.core.micro;

import com.nubeiot.core.component.IComponentProvider;
import com.nubeiot.core.utils.Configs;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface IMicroProvider extends IComponentProvider<Microservice> {

    static Microservice initConfig(Vertx vertx, JsonObject allConfig) {
        JsonObject defMicroCfg = Configs.getSystemCfg(Configs.loadDefaultConfig("micro.json"))
                                        .getJsonObject(Microservice.MICRO_CFG_NAME, new JsonObject());
        JsonObject inputMicroCfg = Configs.getSystemCfg(allConfig)
                                          .getJsonObject(Microservice.MICRO_CFG_NAME, new JsonObject());
        return new Microservice(vertx, defMicroCfg.mergeIn(inputMicroCfg, true));
    }

}
