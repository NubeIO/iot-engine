package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Abstract service to implement {@code CRUD} listeners for entity
 */
@RequiredArgsConstructor
public abstract class AbstractEntityService<P extends VertxPojo, M extends EntityMetadata>
    implements SimpleEntityService<P, M>, EntityTransformer {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final EntityHandler entityHandler;

    @Override
    public EntityHandler entityHandler() {
        return entityHandler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull SimpleQueryExecutor<P> queryExecutor() {
        return SimpleQueryExecutor.create(entityHandler(), context());
    }

    @Override
    public @NonNull EntityValidation validation() {
        return context();
    }

    @Override
    public @NonNull EntityTransformer transformer() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = onHandlingManyResource(requestData);
        return queryExecutor().findMany(reqData)
                              .map(m -> transformer().afterEachList(m, reqData))
                              .collect(JsonArray::new, JsonArray::add)
                              .map(this::combineListData)
                              .doOnSuccess(j -> asyncPostService().onSuccess(EventAction.GET_LIST, j))
                              .doOnError(t -> asyncPostService().onError(EventAction.GET_LIST, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = onHandlingOneResource(requestData);
        return queryExecutor().findOneByKey(reqData)
                              .map(pojo -> transformer().afterGet(pojo, reqData))
                              .doOnSuccess(j -> asyncPostService().onSuccess(EventAction.GET_ONE, j))
                              .doOnError(t -> asyncPostService().onError(EventAction.GET_ONE, t));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = onHandlingNewResource(requestData);
        return queryExecutor().insertReturningPrimary((P) validation().onCreating(reqData), reqData)
                              .flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterCreate))
                              .doOnSuccess(j -> asyncPostService().onSuccess(EventAction.CREATE, j))
                              .doOnError(t -> asyncPostService().onError(EventAction.CREATE, t));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        RequestData reqData = onHandlingOneResource(requestData);
        return queryExecutor().modifyReturningPrimary(reqData, EventAction.UPDATE, validation()::onUpdating)
                              .flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterUpdate))
                              .doOnSuccess(j -> asyncPostService().onSuccess(EventAction.UPDATE, j))
                              .doOnError(t -> asyncPostService().onError(EventAction.UPDATE, t));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        RequestData reqData = onHandlingOneResource(requestData);
        return queryExecutor().modifyReturningPrimary(reqData, EventAction.PATCH, validation()::onPatching)
                              .flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterPatch))
                              .doOnSuccess(j -> asyncPostService().onSuccess(EventAction.PATCH, j))
                              .doOnError(t -> asyncPostService().onError(EventAction.PATCH, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        RequestData reqData = onHandlingOneResource(requestData);
        return queryExecutor().deleteOneByKey(reqData)
                              .doOnSuccess(p -> asyncPostService().onSuccess(EventAction.REMOVE, p.toJson()))
                              .doOnError(t -> asyncPostService().onError(EventAction.REMOVE, t))
                              .map(p -> transformer().afterDelete(p, reqData));
    }

    protected JsonObject combineListData(JsonArray results) {
        return new JsonObject().put(context().pluralKeyName(), results);
    }

}
