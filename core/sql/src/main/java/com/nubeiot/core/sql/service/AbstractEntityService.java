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
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Abstract service to implement {@code CRUD} listeners for entity
 */
@RequiredArgsConstructor
public abstract class AbstractEntityService<P extends VertxPojo, M extends EntityMetadata, V extends EntityValidation>
    implements InternalEntityService<P, M, V> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final EntityHandler entityHandler;

    @Override
    public EntityHandler entityHandler() {
        return entityHandler;
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_ONE, requestData);
        return queryExecutor().findOneByKey(reqData)
                              .map(pojo -> transformer().afterGet(pojo, reqData))
                              .doOnSuccess(j -> postService().onSuccess(EventAction.GET_ONE, j))
                              .doOnError(t -> postService().onError(EventAction.GET_ONE, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_LIST, requestData);
        return queryExecutor().findMany(reqData)
                              .map(m -> transformer().afterEachList(m, reqData))
                              .collect(JsonArray::new, JsonArray::add)
                              .map(results -> new JsonObject().put(metadata().pluralKeyName(), results))
                              .doOnSuccess(j -> postService().onSuccess(EventAction.GET_LIST, j))
                              .doOnError(t -> postService().onError(EventAction.GET_LIST, t));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = recompute(EventAction.CREATE, requestData);
        final P pojo = (P) validation().onCreate(metadata().parse(reqData.body()), reqData.headers());
        return queryExecutor().insertReturningPrimary(pojo, reqData)
                              .flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterCreate))
                              .doOnSuccess(j -> postService().onSuccess(EventAction.CREATE, j))
                              .doOnError(t -> postService().onError(EventAction.CREATE, t));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        RequestData reqData = recompute(EventAction.UPDATE, requestData);
        return queryExecutor().modifyReturningPrimary(reqData, EventAction.UPDATE, validation()::onUpdate)
                              .flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterUpdate))
                              .doOnSuccess(j -> postService().onSuccess(EventAction.UPDATE, j))
                              .doOnError(t -> postService().onError(EventAction.UPDATE, t));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        RequestData reqData = recompute(EventAction.PATCH, requestData);
        return queryExecutor().modifyReturningPrimary(reqData, EventAction.PATCH, validation()::onPatch)
                              .flatMap(pk -> responseByLookupKey(pk, reqData, transformer()::afterPatch))
                              .doOnSuccess(j -> postService().onSuccess(EventAction.PATCH, j))
                              .doOnError(t -> postService().onError(EventAction.PATCH, t));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        RequestData reqData = recompute(EventAction.REMOVE, requestData);
        final Object pk = metadata().parseKey(reqData);
        return queryExecutor().deleteOneByKey(reqData)
                              .doOnSuccess(p -> postService().onSuccess(EventAction.REMOVE, p.toJson()))
                              .doOnError(t -> postService().onError(EventAction.REMOVE, t))
                              .flatMapSingle(p -> transformer().response(metadata().requestKeyName(), pk,
                                                                         () -> transformer().afterDelete(p, reqData)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull SimpleQueryExecutor<P> queryExecutor() {
        return SimpleQueryExecutor.create(entityHandler(), metadata());
    }

}
