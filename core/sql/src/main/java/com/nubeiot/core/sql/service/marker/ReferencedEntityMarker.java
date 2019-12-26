package com.nubeiot.core.sql.service.marker;

import lombok.NonNull;

/**
 * Represents for an {@code database entity} marker.
 * <p>
 * It manifests {@code resource entity} is referenced by to other resources. In {@code database layer}, it is known as
 * {@code primary key} of the {@code table} is {@code foreign key} in other {@code tables}.
 *
 * @since 1.0.0
 */
public interface ReferencedEntityMarker extends EntityMarker {

    /**
     * Defines {@code referencing entities} of this {@code resource entity}
     *
     * @return referencing entities
     * @see EntityReferences
     * @since 1.0.0
     */
    @NonNull EntityReferences referencingEntities();

}
