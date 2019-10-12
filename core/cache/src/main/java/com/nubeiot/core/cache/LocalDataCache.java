package com.nubeiot.core.cache;

public interface LocalDataCache<K, V> extends LocalCache<K, V> {

    LocalDataCache add(K key, V v);

    V remove(K key);

}
