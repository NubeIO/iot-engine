package com.nubeio.iot.share;

import com.nubeio.iot.share.utils.Configs;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface IMicroVerticle {

    static MicroserviceConfig initConfig(Vertx vertx, JsonObject allConfig) {
        JsonObject defMicroCfg = Configs.getSystemCfg(Configs.loadDefaultConfig("micro.json"))
                                        .getJsonObject(MicroserviceConfig.MICRO_CFG_NAME, new JsonObject());
        JsonObject inputMicroCfg = Configs.getSystemCfg(allConfig)
                                          .getJsonObject(MicroserviceConfig.MICRO_CFG_NAME, new JsonObject());
        return new MicroserviceConfig(vertx, defMicroCfg.mergeIn(inputMicroCfg, true));
    }

    MicroserviceConfig getMicroserviceConfig();

}
