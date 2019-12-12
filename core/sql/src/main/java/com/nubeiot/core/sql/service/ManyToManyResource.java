package com.nubeiot.core.sql.service;

import java.util.Collections;
import java.util.List;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

/**
 * Represents for an {@code database entity} marker.
 * <p>
 * It manifests a relationship between more than one {@code database entities}, in-which both sides can relate to
 * multiple instances of the other side
 *
 * @since 1.0.0
 */
public interface ManyToManyResource {

    /**
     * Represents physical database entity
     *
     * @return physical entity metadata
     * @apiNote It represents for a joining table in {@code many-to-many} relationship
     * @since 1.0.0
     */
    @NonNull CompositeMetadata context();

    /**
     * Represents logical database entity
     *
     * @return logical entity metadata
     * @apiNote Represents one reference table in {@code many-to-many} relationship that is actual {@code service
     *     resource context}
     * @since 1.0.0
     */
    @NonNull EntityMetadata reference();

    /**
     * Represents logical database entities
     *
     * @return logical entities metadata
     * @apiNote Represents list of reference tables in {@code many-to-many} relationship that is actual {@code
     *     service resource context}
     * @since 1.0.0
     */
    default @NonNull List<EntityMetadata> references() {
        return Collections.singletonList(reference());
    }

    /**
     * Represents presentation resource of service
     *
     * @return presentation entity metadata
     * @apiNote Represents one reference table in {@code many-to-many} relationship that is {@code service resource
     *     presentation}
     * @since 1.0.0
     */
    @NonNull EntityMetadata resource();

}
