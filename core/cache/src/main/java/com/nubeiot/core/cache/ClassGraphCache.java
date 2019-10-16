package com.nubeiot.core.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import lombok.NonNull;

public final class ClassGraphCache<K, T> implements LocalCache<K, Class<T>> {

    private static final Logger logger = LoggerFactory.getLogger(ClassGraphCache.class);
    private final ConcurrentMap<K, Class<T>> cache = new ConcurrentHashMap<>();
    private Function<K, Class<T>> discover;

    @Override
    public Class<T> get(@NonNull K key) {
        Class<T> val = cache.get(key);
        if (Objects.isNull(discover) || Objects.nonNull(val)) {
            logger.info("Get class by {} from cache", key);
            return val;
        }
        logger.info("Find class by {} then put into cache", key);
        return cache.computeIfAbsent(key, discover);
    }

    @Override
    public Class<T> remove(@NonNull K key) {
        return cache.remove(key);
    }

    @Override
    public Map<K, Class<T>> all() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public ClassGraphCache register(Function<K, Class<T>> discover) {
        this.discover = discover;
        return this;
    }

}
