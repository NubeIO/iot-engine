package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ReferenceEntityTransformer;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;

import lombok.NonNull;

/**
 * Represents service that holds a {@code Resource entity} contains one or more {@code reference} to other resources.
 * <p>
 * It means the {@code service context resource} is in {@code one-to-one} or {@code one-to-many} relationship to another
 * resource. In mapping to database layer, context table has reference key to another table
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see EntityService
 * @see ReferencingEntityMarker
 * @since 1.0.0
 */
public interface OneToManyEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends SimpleEntityService<P, M>, ReferencingEntityMarker {

    /**
     * @return reference query executor
     * @see ReferenceQueryExecutor
     */
    @Override
    @SuppressWarnings("unchecked")
    default @NonNull ReferenceQueryExecutor<P> queryExecutor() {
        return ReferenceQueryExecutor.create(entityHandler(), context(), this);
    }

    /**
     * @return reference entity transformer
     * @see ReferenceEntityTransformer
     */
    @Override
    @NonNull ReferenceEntityTransformer transformer();

}
