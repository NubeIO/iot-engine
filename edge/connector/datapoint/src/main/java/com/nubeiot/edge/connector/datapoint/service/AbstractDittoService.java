package com.nubeiot.edge.connector.datapoint.service;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.AbstractEntityService;
import com.nubeiot.core.sql.EntityHandler;

import lombok.NonNull;

/**
 * {@inheritDoc}
 */
abstract class AbstractDittoService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    extends AbstractEntityService<K, M, R, D> implements DittoService {

    public AbstractDittoService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected final boolean enableTimeAudit() {
        return true;
    }

}
