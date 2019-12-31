package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ReferencingEntityTransformer;
import com.nubeiot.core.sql.query.ReferencingQueryExecutor;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource entity} contains one or more {@code reference} to other resources.
 * <p>
 * It means the {@code service context resource} has an {@code one-to-one} or {@code many-to-one} relationship to
 * another resource.
 * <p>
 * In mapping to {@code database term}, the current {@code table} has the {@code foreign key} to another {@code table}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see EntityService
 * @see ReferencingEntityMarker
 * @since 1.0.0
 */
public interface ReferencingEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends SimpleEntityService<P, M>, ReferencingEntityMarker {

    /**
     * @return reference query executor
     * @see ReferencingQueryExecutor
     */
    @Override
    @SuppressWarnings("unchecked")
    default @NonNull ReferencingQueryExecutor<P> queryExecutor() {
        return ReferencingQueryExecutor.create(entityHandler(), context(), this);
    }

    /**
     * @return reference entity transformer
     * @see ReferencingEntityTransformer
     */
    @Override
    @NonNull ReferencingEntityTransformer transformer();

}
