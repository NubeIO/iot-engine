package com.nubeiot.core.cache;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.vertx.core.Vertx;

import lombok.NonNull;

public interface CacheInitializer<R extends CacheInitializer, C> {

    @NonNull R init(@NonNull C context);

    @SuppressWarnings("unchecked")
    default <T> void addBlockingCache(@NonNull Vertx vertx, @NonNull String cacheKey,
                                      @NonNull Supplier<T> blockingCacheProvider,
                                      @NonNull BiConsumer<String, T> addSharedDataFunc) {
        vertx.executeBlocking(future -> future.complete(blockingCacheProvider.get()),
                              result -> addSharedDataFunc.accept(cacheKey, (T) result.result()));
    }

}
