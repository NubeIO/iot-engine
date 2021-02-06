package com.nubeiot.edge.connector.bacnet.service.subscriber;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.utils.Reflections.ReflectionClass;
import io.vertx.core.Vertx;

import com.nubeiot.edge.connector.bacnet.service.OutboundBACnetCoordinator;

import lombok.NonNull;

public interface BACnetRpcClientHelper {

    static Set<? extends OutboundBACnetCoordinator> createSubscribers(@NonNull Vertx vertx, @NonNull String sharedKey) {
        final Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(Vertx.class, vertx);
        inputs.put(String.class, sharedKey);
        return ReflectionClass.stream(BACnetRpcClientHelper.class.getPackage().getName(),
                                      OutboundBACnetCoordinator.class, ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

}
