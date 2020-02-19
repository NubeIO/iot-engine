package com.nubeiot.edge.connector.bacnet.cache;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;

import lombok.NonNull;

//TODO implement it
public final class BACnetObjectCache extends AbstractLocalCache<CommunicationProtocol, BACnetDevice, BACnetDeviceCache>
    implements LocalDataCache<CommunicationProtocol, BACnetDevice> {

    @Override
    protected String keyLabel() {
        return null;
    }

    @Override
    protected String valueLabel() {
        return null;
    }

    @Override
    public LocalDataCache add(@NonNull CommunicationProtocol key, BACnetDevice device) {
        return null;
    }

}
