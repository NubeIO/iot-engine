package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.query.TransitiveReferenceQueryExecutor;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code one-to-many entity} with {@code transitive
 * resource}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see TransitiveReferenceMarker
 * @see AbstractOneToManyEntityService
 * @since 1.0.0
 */
public abstract class AbstractTransitiveEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends AbstractOneToManyEntityService<P, M> implements TransitiveReferenceMarker {

    /**
     * Instantiates a new Abstract one to many entity service.
     *
     * @param entityHandler the entity handler
     * @since 1.0.0
     */
    public AbstractTransitiveEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull TransitiveReferenceQueryExecutor<P> queryExecutor() {
        return TransitiveReferenceQueryExecutor.create(entityHandler(), context(), this);
    }

    @Override
    public TransitiveReferenceMarker marker() {
        return this;
    }

}
