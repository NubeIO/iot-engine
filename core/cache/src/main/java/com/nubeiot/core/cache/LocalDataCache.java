package com.nubeiot.core.cache;

import lombok.NonNull;

public interface LocalDataCache<K, V> extends LocalCache<K, V> {

    LocalDataCache add(@NonNull K key, V v);

}
