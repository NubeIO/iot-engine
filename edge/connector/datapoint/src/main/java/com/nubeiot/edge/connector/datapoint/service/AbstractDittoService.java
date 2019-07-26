package com.nubeiot.edge.connector.datapoint.service;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.AbstractModelService;

abstract class AbstractDittoService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    extends AbstractModelService<K, M, R, D> implements DittoService {

    AbstractDittoService(D dao) {
        super(dao);
    }

    @Override
    protected final boolean enableTimeAudit() {
        return true;
    }

}
