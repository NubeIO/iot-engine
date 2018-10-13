package com.nubeio.iot.share;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface IClusterVerticle {

    static ClusterConfig initConfig(Vertx vertx, JsonObject config) {
        return new ClusterConfig(vertx, config);
    }

    ClusterConfig getClusterConfig();

    default void start() throws Exception {

    }
}
