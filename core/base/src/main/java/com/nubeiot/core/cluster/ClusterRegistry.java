package com.nubeiot.core.cluster;

import java.util.EnumMap;
import java.util.Objects;

import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClusterRegistry {

    private static final String DEFAULT_CLUSTER_PACKAGE = "com.nubeiot.core.cluster";
    private static ClusterRegistry instance;
    private final EnumMap<ClusterType, IClusterDelegate> registry = new EnumMap<>(ClusterType.class);

    public static synchronized void init() {
        if (Objects.nonNull(instance)) {
            throw new IllegalStateException("Machine is already initialized");
        }
        instance = new ClusterRegistry();
        ReflectionClass.find(DEFAULT_CLUSTER_PACKAGE, IClusterDelegate.class, ClusterDelegate.class)
                       .parallelStream()
                       .forEach(instance::addDelegate);
    }

    public static ClusterRegistry instance() {
        return instance;
    }

    private void addDelegate(Class<IClusterDelegate> delegate) {
        IClusterDelegate clusterDelegate = ReflectionClass.createObject(delegate);
        if (Objects.nonNull(clusterDelegate)) {
            this.registry.put(clusterDelegate.getTypeName(), clusterDelegate);
        }
    }

    public IClusterDelegate getClusterDelegate(ClusterType clusterType) {
        return registry.get(clusterType);
    }

}
