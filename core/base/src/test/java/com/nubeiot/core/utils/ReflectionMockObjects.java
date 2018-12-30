package com.nubeiot.core.utils;

import java.util.List;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.cluster.ClusterDelegate;
import com.nubeiot.core.cluster.ClusterNode;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.exceptions.ServiceException;

import io.vertx.core.spi.cluster.ClusterManager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

class ReflectionMockObjects {

    @Getter
    @RequiredArgsConstructor
    static class MockReflection {

        private final String id;
        @Setter
        private String name;

        public int methodNoArgument() {
            return 1;
        }

        public void throwNubeException(String hey) {
            throw new ServiceException(hey);
        }

        public void throwUnknownException(String hey) {
            throw new RuntimeException(hey);
        }

    }


    @NoArgsConstructor
    @ClusterDelegate
    static class MockAnnotationType implements IClusterDelegate {

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


    static class MockParent {}


    static class MockChild extends MockParent {}

}
