package com.nubeiot.core.sql.service;

import java.util.Map;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents for an {@code database entity} marker.
 * <p>
 * {@code Transitive reference entity} represents for a case:
 * <ul>
 *  <li>Table A has <i>reference field</i> to Table B</li>
 *  <li>Table B has <i>reference field</i> to Table C</li>
 *  <li>Table A has <b>transitive reference</b> to Table C</li>
 * </ul>
 *
 * @see HasReferenceMarker
 * @since 1.0.0
 */
public interface TransitiveReferenceMarker extends HasReferenceMarker {

    /**
     * Declares transitive references mapping.
     * <p>
     * Each mapping key is one of entity metadata in {@link #entityReferences()}, and a corresponding mapping value is
     * reference of this metadata
     *
     * @return transitive references
     * @see EntityMetadata
     * @see TransitiveEntity
     * @since 1.0.0
     */
    @NonNull Map<EntityMetadata, TransitiveEntity> transitiveReferences();

    /**
     * Represents for Transitive entity.
     *
     * @since 1.0.0
     */
    @Getter
    @RequiredArgsConstructor
    class TransitiveEntity {

        /**
         * Defines search entity context
         *
         * @since 1.0.0
         */
        @NonNull
        private final EntityMetadata context;
        /**
         * Defines entity references
         *
         * @see EntityReferences
         * @since 1.0.0
         */
        @NonNull
        private final EntityReferences references;

    }

}
