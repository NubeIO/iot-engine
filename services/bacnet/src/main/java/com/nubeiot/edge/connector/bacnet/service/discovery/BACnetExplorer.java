package com.nubeiot.edge.connector.bacnet.service.discovery;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.utils.Reflections.ReflectionClass;

import com.nubeiot.core.rpc.discovery.RpcExplorerApis;
import com.nubeiot.edge.connector.bacnet.entity.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

public interface BACnetExplorer<P extends IoTEntity> extends BACnetApis, BACnetProtocol, RpcExplorerApis<P> {

    static Set<? extends BACnetExplorer> createServices(@NonNull SharedDataLocalProxy sharedDataProxy) {
        final Map<Class, Object> inputs = Collections.singletonMap(SharedDataLocalProxy.class, sharedDataProxy);
        return ReflectionClass.stream(BACnetExplorer.class.getPackage().getName(), BACnetExplorer.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    @Override
    default String basePath() {
        return "/discovery";
    }

    @Override
    default String api() {
        return "bacnet.discover." + getClass().getSimpleName();
    }

    @Override
    default @NonNull String destination() {
        return "bacnet.subscription.manager";
    }

}
