package com.nubeiot.edge.module.datapoint.cache;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.nubeiot.core.cache.LIFOCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class PointHistoryCache
    implements LocalDataCache<UUID, PointHistoryData>, LIFOCache<UUID, PointHistoryData> {

    private final ConcurrentMap<UUID, PointHistoryData> cache = new ConcurrentHashMap<>();

    @Override
    public PointHistoryCache add(@NonNull UUID key, PointHistoryData pointHistoryData) {
        cache.put(key, pointHistoryData);
        return this;
    }

    @Override
    public PointHistoryData get(@NonNull UUID key) {
        return cache.get(key);
    }

    @Override
    public PointHistoryData remove(@NonNull UUID key) {
        return cache.remove(key);
    }

    @Override
    public Map<UUID, PointHistoryData> all() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public PointHistoryCache register(Function<UUID, PointHistoryData> discover) {
        return this;
    }

    @Override
    public PointHistoryData first(@NonNull UUID key) {
        return get(key);
    }

    @Override
    public PointHistoryData pop(@NonNull UUID key) {
        return remove(key);
    }

}
