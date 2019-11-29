package com.nubeiot.core.sql.service;

import java.util.stream.Stream;

import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ManyToManyEntityTransformer;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.validation.CompositeValidation;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

public abstract class AbstractManyToManyEntityService<P extends CompositePojo, M extends CompositeMetadata>
    extends AbstractOneToManyEntityService<P, M>
    implements ManyToManyReferenceEntityService<P, M>, ManyToManyEntityTransformer {

    public AbstractManyToManyEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public abstract M context();

    @Override
    public @NonNull CompositeValidation validation() { return this.context(); }

    @Override
    public @NonNull EntityMetadata resourceMetadata() {
        return resource();
    }

    @Override
    public @NonNull ComplexQueryExecutor<P> queryExecutor() {
        return ManyToManyReferenceEntityService.super.queryExecutor();
    }

    @Override
    public @NonNull ManyToManyEntityTransformer transformer() { return this; }

    @Override
    protected OperationValidator getCreationValidator() {
        return OperationValidator.create((req, pojo) -> Single.just(context().onCreating(req)));
    }

    @Override
    @NonNull
    public RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, references()));
    }

    @Override
    @NonNull
    public RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData,
                                                            Stream.concat(references().stream(), Stream.of(resource()))
                                                                  .toArray(EntityMetadata[]::new)));
    }

    @Override
    @NonNull
    public RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, references()));
    }

    @Override
    public @NonNull RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData,
                                                            Stream.concat(references().stream(), Stream.of(resource()))
                                                                  .toArray(EntityMetadata[]::new)));
    }

    @Override
    public EntityReferences entityReferences() {
        final EntityReferences entityReferences = new EntityReferences();
        references().forEach(entityReferences::add);
        return entityReferences.add(resource());
    }

}
