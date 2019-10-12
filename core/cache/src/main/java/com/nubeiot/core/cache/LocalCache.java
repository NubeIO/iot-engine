package com.nubeiot.core.cache;

import java.util.function.Function;

import lombok.NonNull;

public interface LocalCache<K, V> extends Cache {

    V get(@NonNull K key);

    V remove(@NonNull K key);

    LocalCache register(Function<K, V> discover);

}
