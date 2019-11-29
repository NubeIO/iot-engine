package com.nubeiot.core.cache;

public final class ClassGraphCache<K, T> extends AbstractLocalCache<K, Class<T>, ClassGraphCache> {

    @Override
    protected String keyLabel() {
        return null;
    }

    @Override
    protected String valueLabel() {
        return "class";
    }

}
