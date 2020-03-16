package com.nubeiot.core.sql.service;

import io.vertx.core.shareddata.Shareable;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface EntityApiService extends Shareable {

    String DATA_KEY = "ENTITY_API_SERVICE";

    @NonNull String prefixServiceName();

    @NonNull
    default String lookupApiName(@NonNull EntityMetadata metadata) {
        return prefixServiceName() + "." + metadata.modelClass().getSimpleName();
    }

}
