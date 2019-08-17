package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;

/**
 * {@inheritDoc}
 */
abstract class AbstractDataPointService<M extends EntityMetadata, V extends EntityValidation>
    extends AbstractEntityService<M, V> implements DataPointService<M, V> {

    AbstractDataPointService(@NonNull AbstractEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityTransformer transformer() {
        return this;
    }

}
