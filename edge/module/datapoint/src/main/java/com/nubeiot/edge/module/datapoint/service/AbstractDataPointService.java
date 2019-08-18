package com.nubeiot.edge.module.datapoint.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;

/**
 * {@inheritDoc}
 */
abstract class AbstractDataPointService<P extends VertxPojo, M extends EntityMetadata, V extends EntityValidation>
    extends AbstractEntityService<P, M, V> implements DataPointService<P, M, V> {

    AbstractDataPointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityTransformer transformer() {
        return this;
    }

}
