package com.nubeiot.core.sql.service;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface EntityApiService {

    @NonNull String prefixServiceName();

    @NonNull
    default String lookupApiName(@NonNull EntityMetadata metadata) {
        return prefixServiceName() + "." + metadata.modelClass().getSimpleName();
    }

}
