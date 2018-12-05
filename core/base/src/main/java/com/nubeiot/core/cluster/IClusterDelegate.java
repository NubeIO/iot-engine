package com.nubeiot.core.cluster;

import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import lombok.NonNull;

public interface IClusterDelegate {

    @NonNull String getTypeName();

    @NonNull ClusterManager initClusterManager(JsonObject clusterConfig);

}
