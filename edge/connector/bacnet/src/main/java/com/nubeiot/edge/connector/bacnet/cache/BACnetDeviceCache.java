package com.nubeiot.edge.connector.bacnet.cache;

import io.vertx.core.Vertx;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDeviceInitializer;
import com.nubeiot.edge.connector.bacnet.IBACnetDevice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

//TODO implement it
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BACnetDeviceCache extends AbstractLocalCache<CommunicationProtocol, IBACnetDevice, BACnetDeviceCache>
    implements LocalDataCache<CommunicationProtocol, IBACnetDevice> {

    static BACnetDeviceCache init(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetDeviceCache().register(protocol -> BACnetDeviceInitializer.builder()
                                                                                   .vertx(vertx)
                                                                                   .sharedKey(sharedKey)
                                                                                   .build()
                                                                                   .asyncStart(protocol));
    }

    @Override
    public BACnetDeviceCache add(@NonNull CommunicationProtocol protocol, IBACnetDevice device) {
        cache().put(protocol, device);
        return this;
    }

    @Override
    protected String keyLabel() {
        return CommunicationProtocol.class.getName();
    }

    @Override
    protected String valueLabel() {
        return IBACnetDevice.class.getSimpleName();
    }

}
