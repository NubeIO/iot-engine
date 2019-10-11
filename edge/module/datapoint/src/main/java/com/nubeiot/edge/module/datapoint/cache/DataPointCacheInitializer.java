package com.nubeiot.edge.module.datapoint.cache;

import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.nubeiot.core.cache.CacheInitializer;
import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel;
import com.nubeiot.iotdata.unit.DataType;

import lombok.NonNull;

public final class DataPointCacheInitializer implements CacheInitializer<DataPointCacheInitializer, EntityHandler> {

    private static String CACHE_DATA_TYPE = "CACHE_DATA_TYPE";

    @Override
    public DataPointCacheInitializer init(EntityHandler context) {
        addBlockingCache(context, IDittoModel.CACHE_SYNC_CLASSES,
                         () -> new ClassGraphCache<EntityMetadata>().register(IDittoModel::find));
        addBlockingCache(context, CACHE_DATA_TYPE,
                         () -> Collections.unmodifiableSet(DataType.available().collect(Collectors.toSet())));
        return this;
    }

    private <T> void addBlockingCache(@NonNull EntityHandler context, @NonNull String cacheKey,
                                      @NonNull Supplier<T> blockingCacheProvider) {
        context.vertx()
               .executeBlocking(future -> future.complete(blockingCacheProvider.get()),
                                result -> context.addSharedData(cacheKey, result.result()));
    }

}
