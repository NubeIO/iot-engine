package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.service.marker.ReferencedEntityMarker;

/**
 * The interface Referenced entity service.
 *
 * @param <P>  Type of {@code VertxPojo}
 * @param <M>  Type of {@code EntityMetadata}
 * @param <CP> Type of {@code CompositePojo}
 * @param <CM> Type of {@code CompositeMetadata}
 * @see ReferencedEntityMarker
 * @since 1.0.0
 */
public interface ReferencedEntityService<P extends VertxPojo, M extends EntityMetadata, CP extends CompositePojo<P, CP>, CM extends CompositeMetadata>
    extends BaseEntityService<M>, ReferencedEntityMarker {

    /**
     * Declares the referenced context metadata.
     *
     * @return referenced context metadata
     * @see CompositeMetadata
     * @since 1.0.0
     */
    CM referencedContext();

    /**
     * Declares referenced query executor.
     *
     * @return referenced query executor
     * @see ComplexQueryExecutor
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default ComplexQueryExecutor<CP> referencedQuery() {
        return ComplexQueryExecutor.create(entityHandler()).from(referencedContext()).references(dependantEntities());
    }

}
