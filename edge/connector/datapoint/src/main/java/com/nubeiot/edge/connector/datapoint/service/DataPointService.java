package com.nubeiot.edge.connector.datapoint.service;

import java.util.Collections;
import java.util.Map;

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
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.AbstractEntityService;
import com.nubeiot.core.sql.EntityHandler;

import lombok.NonNull;

/**
 * {@inheritDoc}
 */
abstract class DataPointService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    extends AbstractEntityService<K, M, R, D> implements EventHttpService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HttpClientDelegate client;

    public DataPointService(@NonNull EntityHandler entityHandler, @NonNull HttpClientDelegate client) {
        super(entityHandler);
        this.client = client;
    }

    protected abstract String endpoint();

    @Override
    protected final boolean enableTimeAudit() {
        return true;
    }

    @Override
    protected boolean enableFullResourceInCUDResponse() {
        return true;
    }

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

    @Override
    public final String address() {
        return this.getClass().getName();
    }

    @Override
    public Map<String, EventMethodDefinition> definitions() {
        return Collections.singletonMap(address(), EventMethodDefinition.createDefault(
            "/" + modelClass().getSimpleName().toLowerCase(), "/:" + primaryKeyName()));
    }

    protected void publishData(JsonObject data) {
        RequestData reqData = RequestData.builder().body(data).headers(new JsonObject()).build();
        this.client.execute(endpoint(), HttpMethod.PUT, reqData).subscribe(logger::info, t -> logger.error("error", t));
    }

}
