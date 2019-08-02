package com.nubeiot.edge.connector.datapoint.service;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.AbstractEntityService;
import com.nubeiot.core.sql.EntityHandler;

import lombok.NonNull;

/**
 * {@inheritDoc}
 */
abstract class DataPointService<KEY, MODEL extends VertxPojo, RECORD extends UpdatableRecord<RECORD>,
                                   DAO extends VertxDAO<RECORD, MODEL, KEY>>
    extends AbstractEntityService<KEY, MODEL, RECORD, DAO> implements IDataPointService<KEY, MODEL, RECORD, DAO> {

    private final HttpClientDelegate client;

    public DataPointService(@NonNull EntityHandler entityHandler, @NonNull HttpClientDelegate client) {
        super(entityHandler);
        this.client = client;
    }

    protected abstract String endpoint();

    @Override
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return super.create(requestData).doOnSuccess(this::publishData);
    }

    @Override
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return super.update(requestData).doOnSuccess(this::publishData);
    }

    @Override
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return super.patch(requestData).doOnSuccess(this::publishData);
    }

    @Override
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        return super.delete(requestData).doOnSuccess(this::publishData);
    }

    protected void publishData(JsonObject data) {
        RequestData reqData = RequestData.builder().body(data).headers(new JsonObject()).build();
        this.client.execute(endpoint(), HttpMethod.PUT, reqData).subscribe(logger::info, t -> logger.error("error", t));
    }

}
