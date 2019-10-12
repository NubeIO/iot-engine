package com.nubeiot.core.cache;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import lombok.NonNull;

public final class ClassGraphCache<K> implements LocalCache<K, Class<?>> {

    private final ConcurrentMap<K, Class<?>> cache = new ConcurrentHashMap<>();
    private Function<K, Class<?>> discover;

    @Override
    public Class<?> get(@NonNull K key) {
        Class<?> val = cache.get(key);
        if (Objects.isNull(discover) || Objects.nonNull(val)) {
            return val;
        }
        return cache.computeIfAbsent(key, discover);
    }

    @Override
    public Class<?> remove(@NonNull K key) {
        return cache.remove(key);
    }

    @Override
    public ClassGraphCache register(Function<K, Class<?>> discover) {
        this.discover = discover;
        return this;
    }

}
