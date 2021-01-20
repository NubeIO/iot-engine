package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.utils.Reflections.ReflectionClass;
import io.vertx.core.Vertx;

import com.nubeiot.core.rpc.discovery.RpcDiscoveryApis;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public interface BACnetRpcDiscoveryService<P extends JsonData>
    extends BACnetApis, BACnetRpcProtocol<P>, RpcDiscoveryApis<P, AbstractRpcDiscoveryService> {

    static Set<? extends BACnetRpcDiscoveryService> createServices(@NonNull Vertx vertx, @NonNull String sharedKey) {
        final Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(Vertx.class, vertx);
        inputs.put(String.class, sharedKey);
        return ReflectionClass.stream(BACnetRpcDiscoveryService.class.getPackage().getName(),
                                      BACnetRpcDiscoveryService.class, ReflectionClass.publicClass())
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

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    final class DiscoveryRequestWrapper {

        @NonNull
        private final DiscoverRequest request;
        @NonNull
        private final DiscoverOptions options;
        private final BACnetDevice device;

        public ObjectIdentifier remoteDeviceId() {
            return request.getDeviceCode();
        }

        public ObjectIdentifier objectCode() {
            return request.getObjectId();
        }

    }

}
