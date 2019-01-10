package com.nubeiot.core.utils.mock;

import java.util.List;

import io.vertx.core.spi.cluster.ClusterManager;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.cluster.ClusterDelegate;
import com.nubeiot.core.cluster.ClusterNode;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.exceptions.ServiceException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class ReflectionMockObjects {

    @Getter
    @RequiredArgsConstructor
    public static class MockReflection {

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
    public static class MockAnnotationType implements IClusterDelegate {

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


    public static class MockParent {

        private void mPrivate()            {}

        void mPackage()                    {}

        protected void mProtected()        {}

        public void mPublic()              {}

        public final void mPublicFinal()   {}

        public static void mPublicStatic() {}

    }


    public static class MockChild extends MockParent {

        private void mPrivate()           {}

        private void mChildPrivate()      {}

        void mPackage()                   {}

        protected void mProtected()       {}

        public void mPublic()             {}

        public static void mChildStatic() {}

    }

}
