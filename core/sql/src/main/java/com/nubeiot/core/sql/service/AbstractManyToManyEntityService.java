package com.nubeiot.core.sql.service;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.ManyToManyEntityTransformer;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.validation.CompositeValidation;

import lombok.NonNull;

public abstract class AbstractManyToManyEntityService<P extends CompositePojo, M extends CompositeMetadata>
    extends AbstractOneToManyEntityService<P, M>
    implements ManyToManyReferenceEntityService<P, M>, ManyToManyEntityTransformer {

    public AbstractManyToManyEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull ComplexQueryExecutor<P> queryExecutor() {
        return ManyToManyReferenceEntityService.super.queryExecutor();
    }

    @Override
    public @NonNull ManyToManyEntityTransformer transformer() { return this; }

    @Override
    @NonNull
    public RequestData onHandlingManyResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, reference()));
    }

    @Override
    @NonNull
    public RequestData onHandlingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, reference(), resource()));
    }

    @Override
    public abstract M context();

    @Override
    public @NonNull CompositeValidation validation() { return this.context(); }

    @Override
    public @NonNull String resourcePluralKey() {
        return resource().pluralKeyName();
    }

    @Override
    @NonNull
    public RequestData onHandlingNewResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, reference()));
    }

}
