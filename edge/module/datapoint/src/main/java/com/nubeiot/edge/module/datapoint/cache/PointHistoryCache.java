package com.nubeiot.edge.module.datapoint.cache;

import java.util.UUID;
import java.util.function.Function;

import com.nubeiot.core.cache.LIFOCache;
import com.nubeiot.core.cache.LocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;

import lombok.NonNull;

public class PointHistoryCache implements LocalDataCache<UUID, PointHistoryData>, LIFOCache<UUID, PointHistoryCache> {

    @Override
    public LocalDataCache add(UUID key, PointHistoryData pointHistoryData) {
        return null;
    }

    @Override
    public PointHistoryData remove(UUID key) {
        return null;
    }

    @Override
    public PointHistoryData get(@NonNull UUID key) {
        return null;
    }

    @Override
    public LocalCache register(Function<UUID, PointHistoryData> discover) {
        return null;
    }

    @Override
    public PointHistoryCache pop(UUID key) {
        return null;
    }

    @Override
    public PointHistoryCache first(UUID key) {
        return null;
    }

}
