package com.nubeiot.core.cache;

import java.util.function.Function;

import io.vertx.core.shareddata.Shareable;

import lombok.NonNull;

public interface InternalCache<K, V> extends Cache, Shareable {

    V get(@NonNull K key);

    InternalCache register(Function<K, V> discover);

}
