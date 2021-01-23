package com.nubeiot.core.sql.cache;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface EntityServiceIndex {

    String DATA_KEY = "ENTITY_SERVICE_INDEX";

    @NonNull String lookupApiAddress(@NonNull EntityMetadata metadata);

}
