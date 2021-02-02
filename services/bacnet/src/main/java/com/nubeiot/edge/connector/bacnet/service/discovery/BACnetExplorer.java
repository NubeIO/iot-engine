package com.nubeiot.edge.connector.bacnet.service.discovery;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.iot.connector.discovery.ExplorerServiceApis;
import io.github.zero88.qwe.iot.data.IoTEntities;
import io.github.zero88.utils.Reflections.ReflectionClass;

import com.nubeiot.edge.connector.bacnet.entity.BACnetEntity;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;

import lombok.NonNull;

public interface BACnetExplorer<K, P extends BACnetEntity<K>, X extends IoTEntities<K, P>>
    extends BACnetApis, ExplorerServiceApis<K, P, X> {

    static Set<? extends BACnetExplorer> createServices(@NonNull SharedDataLocalProxy sharedDataProxy) {
        final Map<Class, Object> inputs = Collections.singletonMap(SharedDataLocalProxy.class, sharedDataProxy);
        return ReflectionClass.stream(BACnetExplorer.class.getPackage().getName(), BACnetExplorer.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

}
