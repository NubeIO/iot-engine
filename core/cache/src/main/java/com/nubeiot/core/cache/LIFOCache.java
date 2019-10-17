package com.nubeiot.core.cache;

import lombok.NonNull;

public interface LIFOCache<K, V> extends Cache {

    V first(@NonNull K key);

    V pop(@NonNull K key);

}
