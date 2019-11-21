package com.nubeiot.core.sql.service;

import java.util.Collections;
import java.util.List;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface ManyToManyResource {

    /**
     * Represents physical database entity
     *
     * @return physical entity metadata
     * @apiNote It represents for a joining table in many-to-many relationship
     */
    @NonNull CompositeMetadata context();

    /**
     * Represents logical database entity
     *
     * @return logical entity metadata
     * @apiNote Represents one reference table in {@code many-to-many} relationship that is actual {@code service
     *     resource context}
     */
    @NonNull EntityMetadata reference();

    /**
     * Represents logical database entities
     *
     * @return logical entities metadata
     * @apiNote Represents list of reference tables in {@code many-to-many} relationship that is actual {@code
     *     service resource context}
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
     */
    @NonNull EntityMetadata resource();

}
