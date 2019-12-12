package com.nubeiot.core.sql.service;

import java.util.Set;

/**
 * Represents for an {@code database entity} marker.
 * <p>
 * It manifests one {@code resource entity} has one or more {@code references} to other resources and also includes the
 * {@code reference entity} into itself
 *
 * @see HasReferenceMarker
 * @since 1.0.0
 */
public interface GroupReferenceMarker extends HasReferenceMarker {

    /**
     * Declares {@code group references} for references entities.
     *
     * @return the entity references
     * @since 1.0.0
     */
    EntityReferences groupReferences();

    @Override
    default Set<String> ignoreFields() {
        return groupReferences().ignoreFields();
    }

}
