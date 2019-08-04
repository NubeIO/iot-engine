package com.nubeiot.edge.connector.datapoint.service;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.sql.AbstractEntityService;
import com.nubeiot.core.sql.EntityHandler;

import lombok.NonNull;

/**
 * {@inheritDoc}
 */
abstract class AbstractDataPointService<KEY, MODEL extends VertxPojo, RECORD extends UpdatableRecord<RECORD>,
                                           DAO extends VertxDAO<RECORD, MODEL, KEY>>
    extends AbstractEntityService<KEY, MODEL, RECORD, DAO> implements DataPointService<KEY, MODEL, RECORD, DAO> {

    public AbstractDataPointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return super.create(requestData).doOnSuccess(publisher());
    }

    @Override
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return super.update(requestData).doOnSuccess(publisher());
    }

    @Override
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return super.patch(requestData).doOnSuccess(publisher());
    }

    @Override
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        return super.delete(requestData).doOnSuccess(publisher());
    }

    @Override
    public Publisher publisher() {
        return null;
    }

}
