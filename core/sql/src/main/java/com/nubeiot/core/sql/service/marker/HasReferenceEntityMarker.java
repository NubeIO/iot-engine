package com.nubeiot.core.sql.service.marker;

import lombok.NonNull;

/**
 * Represents for an {@code database entity} marker.
 * <p>
 * It manifests {@code resource entity} has one or more {@code reference} to other resources.
 *
 * @since 1.0.0
 */
public interface HasReferenceEntityMarker extends EntityMarker {

    /**
     * Defines {@code referenced entities} of this {@code resource entity}
     *
     * @return referenced entities
     * @see EntityReferences
     * @since 1.0.0
     */
    @NonNull EntityReferences referencedEntities();

}
