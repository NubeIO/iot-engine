package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.utils.Reflections.ReflectionClass;

import com.nubeiot.core.rpc.discovery.RpcDiscoveryApis;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

public interface BACnetRpcDiscoveryService<P extends IoTEntity>
    extends BACnetApis, BACnetRpcProtocol<P>, RpcDiscoveryApis<P> {

    static Set<? extends BACnetRpcDiscoveryService> createServices(@NonNull SharedDataLocalProxy sharedDataProxy) {
        final Map<Class, Object> inputs = Collections.singletonMap(SharedDataLocalProxy.class, sharedDataProxy);
        return ReflectionClass.stream(BACnetRpcDiscoveryService.class.getPackage().getName(),
                                      BACnetRpcDiscoveryService.class, ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    @Override
    default String basePath() {
        return "/discovery";
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
