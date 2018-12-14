package com.nubeiot.core.cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.utils.Reflections;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClusterRegistry {

    public static final String DEFAULT_CLUSTER = "hazelcast";
    private static final String DEFAULT_CLUSTER_PACKAGE = "com.nubeiot.core.cluster";
    private static ClusterRegistry instance;
    private final Map<String, IClusterDelegate> registry = new HashMap<>();

    public static synchronized void init() {
        if (Objects.nonNull(instance)) {
            throw new IllegalStateException("Machine is already initialized");
        }
        instance = new ClusterRegistry();
        Reflections.scanClassesInPackage(DEFAULT_CLUSTER_PACKAGE, ClusterDelegate.class, IClusterDelegate.class)
                   .parallelStream()
                   .forEach(instance::addDelegate);
    }

    public static ClusterRegistry instance() {
        return instance;
    }

    private void addDelegate(Class<IClusterDelegate> delegate) {
        IClusterDelegate clusterDelegate = Reflections.createObject(delegate);
        if (Objects.nonNull(clusterDelegate)) {
            this.registry.put(clusterDelegate.getTypeName(), clusterDelegate);
        }
    }

    public IClusterDelegate getClusterDelegate(String delegateType) {
        return registry.get(delegateType);
    }

}
