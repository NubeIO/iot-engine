package com.nubeio.iot.share;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface IMicroVerticle {

    static MicroserviceConfig initConfig(Vertx vertx, JsonObject config) {
        return new MicroserviceConfig(vertx, config);
    }

    MicroserviceConfig getMicroserviceConfig();

}
