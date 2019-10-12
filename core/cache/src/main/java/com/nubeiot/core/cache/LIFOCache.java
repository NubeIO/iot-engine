package com.nubeiot.core.cache;

public interface LIFOCache<K, V> extends Cache {

    V pop(K key);

    V first(K key);

}
