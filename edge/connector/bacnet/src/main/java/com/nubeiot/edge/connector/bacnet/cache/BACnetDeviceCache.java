package com.nubeiot.edge.connector.bacnet.cache;

import java.util.Map;
import java.util.function.Function;

import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;

import lombok.NonNull;

public final class BACnetDeviceCache implements LocalDataCache<String, BACnetDevice> {

    @Override
    public BACnetDeviceCache add(@NonNull String key, BACnetDevice device) {
        return null;
    }

    @Override
    public BACnetDevice get(@NonNull String key) {
        return null;
    }

    @Override
    public BACnetDevice remove(@NonNull String key) {
        return null;
    }

    @Override
    public Map<String, BACnetDevice> all() {
        return null;
    }

    @Override
    public BACnetDeviceCache register(Function<String, BACnetDevice> discover) {
        return null;
    }

}
