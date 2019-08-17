package com.nubeiot.core.sql.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Abstract service to implement {@code CRUD} listeners for entity
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public abstract class AbstractEntityService<M extends EntityMetadata, V extends EntityValidation>
    implements InternalEntityService<M, V> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AbstractEntityHandler entityHandler;

    @Override
    public @NonNull AbstractEntityHandler entityHandler() { return entityHandler; }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_LIST, requestData);
        return doGetList(reqData).map(m -> transformer().afterEachList(m, reqData))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put(metadata().pluralKeyName(), results))
                                 .doOnSuccess(j -> postAction().onSuccess(EventAction.GET_LIST, j))
                                 .doOnError(t -> postAction().onError(EventAction.GET_LIST, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_ONE, requestData);
        return doGetOne(reqData).map(pojo -> transformer().afterGet(pojo, reqData))
                                .doOnSuccess(j -> postAction().onSuccess(EventAction.GET_ONE, j))
                                .doOnError(t -> postAction().onError(EventAction.GET_ONE, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = recompute(EventAction.CREATE, requestData);
        return dao().insertReturningPrimary(validation().onCreate(metadata().parse(reqData.body()), reqData.headers()))
                    .flatMap(k -> cudResponse(EventAction.CREATE, k, reqData))
                    .doOnSuccess(j -> postAction().onSuccess(EventAction.CREATE, j))
                    .doOnError(t -> postAction().onError(EventAction.CREATE, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return doUpdate(requestData, EventAction.UPDATE, validation()::onUpdate).doOnSuccess(
            j -> postAction().onSuccess(EventAction.UPDATE, j))
                                                                                .doOnError(t -> postAction().onError(
                                                                                    EventAction.UPDATE, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return doUpdate(requestData, EventAction.PATCH, validation()::onPatch).doOnSuccess(
            j -> postAction().onSuccess(EventAction.PATCH, j))
                                                                              .doOnError(t -> postAction().onError(
                                                                                  EventAction.PATCH, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        RequestData reqData = recompute(EventAction.REMOVE, requestData);
        final Object pk = metadata().parsePrimaryKey(reqData);
        return doGetOne(reqData).flatMap(m -> dao().deleteById(pk)
                                                   .filter(r -> r > 0)
                                                   .switchIfEmpty(Single.error(metadata().notFound(pk)))
                                                   .flatMap(r -> transformer().enableFullResourceInCUDResponse()
                                                                 ? cudResponse(EventAction.REMOVE, m, reqData)
                                                                 : cudResponse(EventAction.REMOVE, pk, reqData)))
                                .doOnSuccess(j -> postAction().onSuccess(EventAction.REMOVE, j))
                                .doOnError(t -> postAction().onError(EventAction.REMOVE, t));
    }

}
