package com.nubeiot.core.sql.cache;

import java.util.Optional;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.core.exceptions.ServiceNotFoundException;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

final class DefaultEntityServiceCache extends AbstractLocalCache<String, String, DefaultEntityServiceCache>
    implements LocalDataCache<String, String>, EntityServiceCacheIndex {

    @Override
    protected @NonNull String keyLabel() {
        return "Entity Metadata";
    }

    @Override
    protected @NonNull String valueLabel() {
        return "Service address";
    }

    @Override
    public DefaultEntityServiceCache add(@NonNull String metadata, @NonNull String serviceAddress) {
        cache().putIfAbsent(metadata, serviceAddress);
        return this;
    }

    @Override
    @NonNull
    public String lookupApiAddress(@NonNull EntityMetadata metadata) {
        return Optional.ofNullable(get(metadata.getClass().getName()))
                       .orElseThrow(() -> new ServiceNotFoundException(
                           "Not found service address of " + metadata.table().getName()));
    }

    @Override
    public EntityServiceCacheIndex add(@NonNull EntityMetadata metadata, @NonNull String serviceAddress) {
        return add(metadata.getClass().getName(), serviceAddress);
    }

}
