package com.nubeiot.edge.module.datapoint.cache;

import java.util.function.Supplier;

import com.fasterxml.jackson.databind.InjectableValues.Std;
import com.nubeiot.core.cache.CacheInitializer;
import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.scheduler.DataJobDefinition;

import lombok.NonNull;

public final class DataCacheInitializer implements CacheInitializer<DataCacheInitializer, EntityHandler> {

    public static final String DATA_TYPE_CACHE = "DATA_TYPE_CACHE";
    public static final String HISTORIES_DATA_CACHE = "HISTORIES_DATA_CACHE";
    public static final String PROTOCOL_DISPATCHER_CACHE = "PROTOCOL_DISPATCHER_CACHE";
    public static final String JOB_CONFIG_CACHE = "JOB_CONFIG_CACHE";

    @Override
    public DataCacheInitializer init(@NonNull EntityHandler context) {
        final ClassGraphCache<String, DataJobDefinition> jobDefinitionCache = new ClassGraphCache<>();
        DataJobDefinition.MAPPER.setInjectableValues(new Std().addValue(JOB_CONFIG_CACHE, jobDefinitionCache));
        addBlockingCache(context, JOB_CONFIG_CACHE, () -> jobDefinitionCache.register(DataJobDefinition::find));
        addBlockingCache(context, HISTORIES_DATA_CACHE, PointHistoryCache::new);
        addBlockingCache(context, PROTOCOL_DISPATCHER_CACHE, () -> ProtocolDispatcherCache.init(context));
        //        addBlockingCache(context, CACHE_DATA_TYPE,
        //                         () -> Collections.unmodifiableSet(DataType.available().collect(Collectors.toSet())));
        return this;
    }

    private <T> void addBlockingCache(@NonNull EntityHandler context, @NonNull String cacheKey,
                                      @NonNull Supplier<T> blockingCacheProvider) {
        context.vertx()
               .executeBlocking(future -> future.complete(blockingCacheProvider.get()),
                                result -> context.addSharedData(cacheKey, result.result()));
    }

}
