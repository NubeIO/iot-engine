package com.nubeiot.edge.connector.bacnet.cache;

import java.util.function.Supplier;

import com.nubeiot.core.cache.CacheInitializer;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;

import lombok.NonNull;

public final class BACnetCacheInitializer implements CacheInitializer<BACnetCacheInitializer, BACnetVerticle> {

    public static final String EDGE_NETWORK_CACHE = "EDGE_NETWORK_CACHE";
    public static final String BACNET_DEVICE_CACHE = "BACNET_DEVICE_CACHE";

    @Override
    public BACnetCacheInitializer init(@NonNull BACnetVerticle context) {
        addBlockingCache(context, EDGE_NETWORK_CACHE, BACnetNetworkCache::init);
        addBlockingCache(context, BACNET_DEVICE_CACHE,
                         () -> BACnetDeviceCache.init(context.getVertx(), context.getSharedKey()));
        return this;
    }

    private <T> void addBlockingCache(@NonNull BACnetVerticle context, @NonNull String cacheKey,
                                      @NonNull Supplier<T> blockingCacheProvider) {
        context.getVertx()
               .executeBlocking(future -> future.complete(blockingCacheProvider.get()),
                                result -> context.addSharedData(cacheKey, result.result()));
    }

}
