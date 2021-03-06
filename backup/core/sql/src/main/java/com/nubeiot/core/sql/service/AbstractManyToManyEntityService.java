package com.nubeiot.core.sql.service;

import io.reactivex.Single;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ManyToManyEntityTransformer;
import com.nubeiot.core.sql.decorator.ManyToManyRequestDecorator;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.service.marker.ManyToManyMarker;
import com.nubeiot.core.sql.validation.CompositeValidation;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code database entity} has a {@code many-to-many}
 * relationship.
 *
 * @param <P> Type of {@code CompositePojo}
 * @param <M> Type of {@code CompositeMetadata}
 * @see ManyToManyEntityService
 * @see ManyToManyRequestDecorator
 * @see ManyToManyEntityTransformer
 * @since 1.0.0
 */
public abstract class AbstractManyToManyEntityService<P extends CompositePojo, M extends CompositeMetadata>
    extends AbstractEntityService<P, M>
    implements ManyToManyEntityService<P, M>, ManyToManyRequestDecorator, ManyToManyEntityTransformer {

    /**
     * Instantiates a new Abstract many to many entity service.
     *
     * @param entityHandler the entity handler
     * @since 1.0.0
     */
    public AbstractManyToManyEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public abstract M context();

    @Override
    public @NonNull ComplexQueryExecutor<P> queryExecutor() {
        return ManyToManyEntityService.super.queryExecutor();
    }

    @Override
    public @NonNull CompositeValidation validation() { return this.context(); }

    @Override
    public @NonNull ManyToManyEntityTransformer transformer() { return this; }

    @Override
    public @NonNull EntityMetadata resourceMetadata() {
        return resource();
    }

    @Override
    protected OperationValidator initCreationValidator() {
        return OperationValidator.create((req, pojo) -> Single.just(context().onCreating(req)));
    }

    @Override
    public ManyToManyMarker marker() { return this; }

}
