package com.nubeiot.core.sql.service;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.validation.CompositeValidation;

import lombok.NonNull;

public abstract class AbstractManyToOneEntityService<P extends CompositePojo, M extends CompositeMetadata,
                                                        V extends CompositeValidation>
    extends AbstractEntityService<P, M, V> implements ManyToOneReferenceEntityService<P, M, V> {

    public AbstractManyToOneEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull ComplexQueryExecutor<P> queryExecutor() {
        return ManyToOneReferenceEntityService.super.queryExecutor();
    }

}
