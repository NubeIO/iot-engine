package com.nubeiot.edge.connector.bacnet.cache;

import io.vertx.core.Vertx;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BACnetDeviceCache extends AbstractLocalCache<CommunicationProtocol, BACnetDevice, BACnetDeviceCache>
    implements LocalDataCache<CommunicationProtocol, BACnetDevice> {

    static BACnetDeviceCache init(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetDeviceCache().register(protocol -> new BACnetDevice(vertx, sharedKey, protocol));
    }

    @Override
    public BACnetDeviceCache add(@NonNull CommunicationProtocol protocol, BACnetDevice device) {
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

}
