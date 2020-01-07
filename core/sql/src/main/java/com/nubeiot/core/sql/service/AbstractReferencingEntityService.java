package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.HasReferenceRequestDecorator;
import com.nubeiot.core.sql.decorator.ReferencingEntityTransformer;
import com.nubeiot.core.sql.query.ReferencingQueryExecutor;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code database entity} has a {@code many-to-one}
 * relationship.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see ReferencingEntityService
 * @see HasReferenceRequestDecorator
 * @see ReferencingEntityTransformer
 * @since 1.0.0
 */
public abstract class AbstractReferencingEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends AbstractEntityService<P, M>
    implements ReferencingEntityService<P, M>, HasReferenceRequestDecorator, ReferencingEntityTransformer {

    /**
     * Instantiates a new Abstract one to many entity service.
     *
     * @param entityHandler the entity handler
     * @since 1.0.0
     */
    public AbstractReferencingEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull ReferencingQueryExecutor<P> queryExecutor() {
        return ReferencingEntityService.super.queryExecutor();
    }

    @Override
    public @NonNull ReferencingEntityTransformer transformer() {
        return this;
    }

    protected OperationValidator initCreationValidator() {
        return OperationValidator.create(
            (req, pojo) -> queryExecutor().checkReferenceExistence(req).map(b -> validation().onCreating(req)));
    }

    @Override
    protected @NonNull OperationValidator initPatchValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onPatching(dbEntity, req)));
    }

    @Override
    protected @NonNull OperationValidator initUpdateValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onUpdating(dbEntity, req)));
    }

    @Override
    public ReferencingEntityMarker marker() {
        return this;
    }

}
