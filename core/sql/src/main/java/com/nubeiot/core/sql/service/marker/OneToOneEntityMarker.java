package com.nubeiot.core.sql.service.marker;

import lombok.NonNull;

/**
 * Represents for an {@code database entity} marker.
 *
 * @since 1.0.0
 */
public interface OneToOneEntityMarker extends ReferencedEntityMarker {

    /**
     * Defines the {@code dependant entities} in {@code one-to-one} relationship of this {@code resource entity}
     *
     * @return dependant entities
     * @see EntityReferences
     * @since 1.0.0
     */
    @NonNull EntityReferences dependantEntities();

}
