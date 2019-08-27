package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Observable;
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
    public EntityHandler entityHandler() { return entityHandler; }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull SimpleQueryExecutor<P> queryExecutor() {
        return SimpleQueryExecutor.create(entityHandler(), context());
    }

    @Override
    public @NonNull EntityValidation validation() { return context(); }

    @Override
    public @NonNull EntityTransformer transformer() { return this; }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = onReadingManyResource(requestData);
        return doGetMany(reqData).map(m -> transformer().afterEachList(m, reqData))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(transformer()::wrapListData)
                                 .doOnSuccess(j -> asyncPostService().onSuccess(this, EventAction.GET_LIST, j))
                                 .doOnError(t -> asyncPostService().onError(this, EventAction.GET_LIST, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = onReadingOneResource(requestData);
        return doGetOne(reqData).map(pojo -> transformer().afterGet(pojo, reqData))
                                .doOnSuccess(j -> asyncPostService().onSuccess(this, EventAction.GET_ONE, j))
                                .doOnError(t -> asyncPostService().onError(this, EventAction.GET_ONE, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = onCreatingOneResource(requestData);
        return doInsert(reqData).flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterCreate))
                                .doOnSuccess(j -> asyncPostService().onSuccess(this, EventAction.CREATE, j))
                                .doOnError(t -> asyncPostService().onError(this, EventAction.CREATE, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        RequestData reqData = onModifyingOneResource(requestData);
        return doUpdate(reqData).flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterUpdate))
                                .doOnSuccess(j -> asyncPostService().onSuccess(this, EventAction.UPDATE, j))
                                .doOnError(t -> asyncPostService().onError(this, EventAction.UPDATE, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        RequestData reqData = onModifyingOneResource(requestData);
        return doPatch(reqData).flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterPatch))
                               .doOnSuccess(j -> asyncPostService().onSuccess(this, EventAction.PATCH, j))
                               .doOnError(t -> asyncPostService().onError(this, EventAction.PATCH, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        RequestData reqData = onModifyingOneResource(requestData);
        return doDelete(reqData).doOnSuccess(p -> asyncPostService().onSuccess(this, EventAction.REMOVE, p.toJson()))
                                .doOnError(t -> asyncPostService().onError(this, EventAction.REMOVE, t))
                                .map(p -> transformer().afterDelete(p, reqData));
    }

    @Override
    public @NonNull EntityMetadata resourceMetadata() {
        return context();
    }

    protected Observable<? extends VertxPojo> doGetMany(RequestData reqData) {
        return queryExecutor().findMany(reqData);
    }

    protected Single<? extends VertxPojo> doGetOne(RequestData reqData) {
        return queryExecutor().findOneByKey(reqData);
    }

    @SuppressWarnings("unchecked")
    protected Single<?> doInsert(RequestData reqData) {
        return queryExecutor().insertReturningPrimary((P) validation().onCreating(reqData), reqData);
    }

    @SuppressWarnings("unchecked")
    protected Single<?> doUpdate(RequestData reqData) {
        return queryExecutor().modifyReturningPrimary(reqData, EventAction.UPDATE, validation()::onUpdating);
    }

    @SuppressWarnings("unchecked")
    protected Single<?> doPatch(RequestData reqData) {
        return queryExecutor().modifyReturningPrimary(reqData, EventAction.PATCH, validation()::onPatching);
    }

    protected Single<P> doDelete(RequestData reqData) {
        return queryExecutor().deleteOneByKey(reqData);
    }

}
