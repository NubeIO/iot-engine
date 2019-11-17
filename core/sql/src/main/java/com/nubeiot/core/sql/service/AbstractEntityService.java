package com.nubeiot.core.sql.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.BiFunction;

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
import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.workflow.TaskExecuter;

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
        return doGetMany(reqData).flatMapSingle(m -> transformer().afterEachList(m, reqData))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(transformer()::wrapListData);
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = onReadingOneResource(requestData);
        return doGetOne(reqData).flatMap(pojo -> transformer().afterGet(pojo, reqData));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = onCreatingOneResource(requestData);
        return doInsert(reqData).flatMap(pk -> afterCreateOrUpdate(reqData, EventAction.CREATE, pk))
                                .flatMap(resp -> transformer().afterCreate(resp.getKey(), resp.getValue(), reqData));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        RequestData reqData = onModifyingOneResource(requestData);
        return doUpdate(reqData).flatMap(pk -> afterCreateOrUpdate(reqData, EventAction.UPDATE, pk))
                                .flatMap(resp -> transformer().afterUpdate(resp.getKey(), resp.getValue(), reqData));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        RequestData reqData = onModifyingOneResource(requestData);
        return doPatch(reqData).flatMap(pk -> afterCreateOrUpdate(reqData, EventAction.PATCH, pk))
                               .flatMap(resp -> transformer().afterPatch(resp.getKey(), resp.getValue(), reqData));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        RequestData reqData = onModifyingOneResource(requestData);
        return doDelete(reqData).doOnEvent((p, t) -> invokeAsyncTask(reqData, EventAction.REMOVE, p, t))
                                .flatMap(p -> transformer().afterDelete(p, reqData));
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

    protected Single<? extends VertxPojo> doLookupByPrimaryKey(@NonNull Object key) {
        return queryExecutor().lookupByPrimaryKey(key);
    }

    @SuppressWarnings("unchecked")
    protected Single<?> doInsert(RequestData reqData) {
        return Single.just((P) validation().onCreating(reqData))
                     .flatMap(p -> executeBeforeTask(reqData, EventAction.CREATE, p))
                     .flatMap(p -> queryExecutor().insertReturningPrimary(p, reqData));
    }

    @SuppressWarnings("unchecked")
    protected Single<?> doUpdate(RequestData reqData) {
        final BiFunction<VertxPojo, RequestData, VertxPojo> validator = validation()::onUpdating;
        return queryExecutor().modifyReturningPrimary(reqData, EventAction.UPDATE, validator.andThen(
            p -> executeBeforeTask(reqData, EventAction.UPDATE, (P) p)));
    }

    @SuppressWarnings("unchecked")
    protected Single<?> doPatch(RequestData reqData) {
        final BiFunction<VertxPojo, RequestData, VertxPojo> validator = validation()::onPatching;
        return queryExecutor().modifyReturningPrimary(reqData, EventAction.PATCH, validator.andThen(
            p -> executeBeforeTask(reqData, EventAction.PATCH, (P) p)));
    }

    protected Single<P> doDelete(RequestData reqData) {
        return queryExecutor().deleteOneByKey(reqData);
    }

    /**
     * Lookup entity pojo then execute async task
     *
     * @param reqData    Request data
     * @param action     Request action
     * @param primaryKey Pojo primary key
     * @return {@code Entry} with key is primary key and value is entity pojo
     */
    protected Single<Entry<?, ? extends VertxPojo>> afterCreateOrUpdate(@NonNull RequestData reqData,
                                                                        @NonNull EventAction action,
                                                                        @NonNull Object primaryKey) {
        return doLookupByPrimaryKey(primaryKey).doOnEvent((p, e) -> invokeAsyncTask(reqData, action, p, e))
                                               .map(pojo -> new SimpleEntry<>(primaryKey, pojo));
    }

    @SuppressWarnings("unchecked")
    protected Single<P> executeBeforeTask(RequestData reqData, EventAction action, P p) {
        if (!taskBeforePersist().isPresent()) {
            return Single.just(p);
        }
        return TaskExecuter.blockingExecute(taskBeforePersist().get(), asyncTaskData(reqData, action, p, null))
                           .switchIfEmpty(Single.just(p));
    }

    @SuppressWarnings("unchecked")
    protected void invokeAsyncTask(@NonNull RequestData reqData, @NonNull EventAction action, VertxPojo pojo,
                                   Throwable t) {
        asyncTaskAfterPersist().ifPresent(
            task -> TaskExecuter.asyncExecute(task, asyncTaskData(reqData, action, pojo, t)));
    }

    protected EntityTaskData<VertxPojo> asyncTaskData(@NonNull RequestData reqData, @NonNull EventAction action,
                                                      VertxPojo pojo, Throwable t) {
        return EntityTaskData.builder()
                             .originReqData(reqData)
                             .originReqAction(action)
                             .metadata(context())
                             .data(pojo)
                             .throwable(t)
                             .build();
    }

}
