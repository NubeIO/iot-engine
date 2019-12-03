package com.nubeiot.edge.connector.bacnet.service.rpc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;

import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;

import lombok.NonNull;

public interface BACnetRpcClientHelper {

    static Set<? extends BACnetSubscriber> createSubscribers(@NonNull Vertx vertx, @NonNull String sharedKey) {
        final Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(Vertx.class, vertx);
        inputs.put(String.class, sharedKey);
        return ReflectionClass.stream(BACnetRpcClientHelper.class.getPackage().getName(), BACnetSubscriber.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    static BACnetNetworkScanner createScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetNetworkScanner(vertx, sharedKey);
    }

}
