package com.nubeiot.core.cluster;

import java.util.List;

import com.nubeiot.core.exceptions.ClusterException;
import com.nubeiot.core.exceptions.NotFoundException;

import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import lombok.NonNull;

public interface IClusterDelegate {

    class Config {

        public static final String LISTENER_ADDRESS = "listener.address";
        public static final String TYPE = "type";
        public static final String ACTIVE = "active";
        public static final String HA = "ha";
        public static final String NAME = "name";

    }

    @NonNull String getTypeName();

    @NonNull ClusterManager initClusterManager(JsonObject clusterConfig);

    /**
     * Find node in cluster.
     *
     * @param id node Id
     * @return cluster node
     * @throws ClusterException  if cluster manager was not initialized
     * @throws NotFoundException if cluster does not have node with given id
     */
    ClusterNode lookupNodeById(String id);

    List<ClusterNode> getAllNodes();

}
