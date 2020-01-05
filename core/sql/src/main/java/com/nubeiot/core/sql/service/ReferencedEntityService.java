package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.service.marker.ReferencedEntityMarker;

import lombok.NonNull;

/**
 * The interface Referenced entity service.
 *
 * @param <CM> Type of {@code CompositeMetadata}
 * @see ReferencedEntityMarker
 * @since 1.0.0
 */
public interface ReferencedEntityService<CM extends CompositeMetadata>
    extends BaseEntityService<CM>, ReferencedEntityMarker {

    /**
     * Declares referenced query executor.
     *
     * @return referenced query executor
     * @see SimpleQueryExecutor
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default <X extends EntityMetadata, Y extends VertxPojo> SimpleQueryExecutor<Y> referencedQuery(
        @NonNull X dependantMetadata) {
        return SimpleQueryExecutor.create(entityHandler(), dependantMetadata);
    }

}
