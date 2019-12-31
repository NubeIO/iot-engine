package com.nubeiot.core.sql.service.marker;

import java.util.function.Predicate;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface OneToOneEntityMarker {

    @NonNull Predicate<EntityMetadata> allowCreation();

}
