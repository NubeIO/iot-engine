package com.nubeiot.core.utils.mock;

import java.util.List;

import io.vertx.core.spi.cluster.ClusterManager;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.cluster.ClusterDelegate;
import com.nubeiot.core.cluster.ClusterNode;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@ClusterDelegate
public class MockAnnotationType implements IClusterDelegate {

    @Override
    public ClusterType getTypeName() {
        return ClusterType.IGNITE;
    }

    @Override
    public ClusterManager initClusterManager(NubeConfig.SystemConfig.ClusterConfig clusterConfig) {
        return null;
    }

    @Override
    public ClusterNode lookupNodeById(String id) {
        return null;
    }

    @Override
    public List<ClusterNode> getAllNodes() {
        return null;
    }

}
