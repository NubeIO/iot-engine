package com.nubeiot.edge.connector.bacnet.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.github.zero88.utils.Strings;
import io.github.zero88.utils.UUID64;
import io.vertx.core.Vertx;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.BACnetDeviceInitializer;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BACnetDeviceCache extends AbstractLocalCache<CommunicationProtocol, BACnetDevice, BACnetDeviceCache>
    implements LocalDataCache<CommunicationProtocol, BACnetDevice> {

    private final ConcurrentMap<CommunicationProtocol, Map<ObjectIdentifier, String>> remoteDeviceCache
        = new ConcurrentHashMap<>();

    static BACnetDeviceCache init(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetDeviceCache().register(protocol -> BACnetDeviceInitializer.builder()
                                                                                   .vertx(vertx)
                                                                                   .sharedKey(sharedKey)
                                                                                   .build()
                                                                                   .asyncStart(protocol));
    }

    @Override
    public BACnetDeviceCache add(@NonNull CommunicationProtocol protocol, @NonNull BACnetDevice device) {
        cache().put(protocol, device);
        return this;
    }

    @Override
    protected String keyLabel() {
        return CommunicationProtocol.class.getName();
    }

    @Override
    protected String valueLabel() {
        return BACnetDevice.class.getSimpleName();
    }

    public BACnetDeviceCache addDataKey(@NonNull CommunicationProtocol protocol,
                                        @NonNull ObjectIdentifier remoteDeviceId, String dataPointDeviceId) {
        final int instance = Optional.of(remoteDeviceId)
                                     .filter(id -> id.getObjectType() == ObjectType.device)
                                     .map(ObjectIdentifier::getInstanceNumber)
                                     .orElseThrow(
                                         () -> new IllegalArgumentException("Invalid remote device identifier"));
        Optional.ofNullable(cache().get(protocol))
                .flatMap(device -> Optional.ofNullable(device.localDevice().getCachedRemoteDevice(instance)))
                .orElseThrow(() -> new NotFoundException(
                    "Invalid or unreachable remote device " + remoteDeviceId + " in protocol " + protocol));
        remoteDeviceCache.computeIfAbsent(protocol, key -> new HashMap<>())
                         .put(remoteDeviceId, UUID64.uuidToBase64(
                             Strings.requireNotBlank(dataPointDeviceId, "Missing data point device_id")));
        return this;
    }

    public Optional<UUID> getDataKey(@NonNull CommunicationProtocol protocol,
                                     @NonNull ObjectIdentifier remoteDeviceId) {
        return Optional.ofNullable(remoteDeviceCache.get(protocol))
                       .flatMap(map -> Optional.ofNullable(map.get(remoteDeviceId)))
                       .map(UUID64::uuid64ToUuid);
    }

}
