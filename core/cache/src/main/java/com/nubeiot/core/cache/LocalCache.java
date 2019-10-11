package com.nubeiot.core.cache;

import java.util.function.Function;

import io.vertx.core.shareddata.Shareable;

import lombok.NonNull;

public interface LocalCache<K, V> extends Cache, Shareable {

    V get(@NonNull K key);

    LocalCache register(Function<K, V> discover);

}
