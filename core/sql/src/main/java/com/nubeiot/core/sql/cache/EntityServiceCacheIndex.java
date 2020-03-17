package com.nubeiot.core.sql.cache;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface EntityServiceCacheIndex extends EntityServiceIndex {

    static EntityServiceCacheIndex create() {
        return new DefaultEntityServiceCache();
    }

    EntityServiceCacheIndex add(@NonNull EntityMetadata metadata, @NonNull String serviceAddress);

}
