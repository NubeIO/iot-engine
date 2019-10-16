package com.nubeiot.edge.module.datapoint.cache;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.nubeiot.core.cache.LocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.iotdata.unit.DataType;

import lombok.NonNull;

public final class DataTypeCache implements LocalDataCache<String, DataType> {

    private final ConcurrentMap<String, DataType> cache = new ConcurrentHashMap<>();

    @Override
    public LocalDataCache add(@NonNull String key, DataType dataType) {
        cache.put(key, dataType);
        return this;
    }

    @Override
    public DataType get(@NonNull String key) {
        return cache.get(key);
    }

    @Override
    public DataType remove(@NonNull String key) {
        return cache.remove(key);
    }

    @Override
    public Map<String, DataType> all() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public LocalCache register(Function<String, DataType> discover) {
        return this;
    }

}
