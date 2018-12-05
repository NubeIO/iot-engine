package com.nubeiot.core.utils;

import com.nubeiot.core.cluster.ClusterDelegate;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.exceptions.ServiceException;

import io.vertx.core.json.JsonObject;
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
        public String getTypeName() {
            return "mock";
        }

        @Override
        public ClusterManager initClusterManager(JsonObject clusterConfig) {
            return null;
        }

    }

}
