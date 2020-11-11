package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.utils.Reflections.ReflectionClass;
import io.vertx.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.nubeiot.edge.module.datapoint.rpc.discovery.DataProtocolDiscoveryApis;

import lombok.NonNull;

public interface BACnetDiscoveryService
    extends BACnetApis, BACnetRpcProtocol, DataProtocolDiscoveryApis<AbstractDiscoveryService> {

    static Set<? extends BACnetDiscoveryService> createServices(@NonNull Vertx vertx, @NonNull String sharedKey) {
        final Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(Vertx.class, vertx);
        inputs.put(String.class, sharedKey);
        return ReflectionClass.stream(BACnetDiscoveryService.class.getPackage().getName(), BACnetDiscoveryService.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.BATCH_CREATE, EventAction.CREATE);
    }

    @Override
    default String api() {
        return "bacnet.discover." + getClass().getSimpleName();
    }

}
