package com.nubeiot.core.cluster;

import java.util.List;

import io.vertx.core.spi.cluster.ClusterManager;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.exceptions.ClusterException;
import com.nubeiot.core.exceptions.NotFoundException;

import lombok.NonNull;

public interface IClusterDelegate {

    @NonNull ClusterType getTypeName();

    @NonNull ClusterManager initClusterManager(NubeConfig.SystemConfig.ClusterConfig clusterConfig);

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
