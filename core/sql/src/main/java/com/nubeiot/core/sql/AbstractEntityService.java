package com.nubeiot.core.sql;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for entity
 */
public abstract class AbstractEntityService<K, P extends VertxPojo, R extends UpdatableRecord<R>,
                                               D extends VertxDAO<R, P, K>, M extends EntityMetadata<K, P, R, D>>
    implements InternalEntityService<K, P, R, D, M> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter(value = AccessLevel.PROTECTED)
    private final EntityHandler entityHandler;

    public AbstractEntityService(@NonNull EntityHandler entityHandler) {
        this.entityHandler = entityHandler;
    }

    @Override
    public @NonNull EntityHandler entityHandler() { return entityHandler; }

    @Override
    public D get() {
        return this.entityHandler.getDao(metadata().daoClass());
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_LIST, requestData);
        return doGetList(reqData).flatMapSingle(m -> Single.just(customizeEachItem(m, reqData)))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put(metadata().listKey(), results));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_ONE, requestData);
        return doGetOne(reqData).map(pojo -> customizeGetItem(pojo, reqData));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = recompute(EventAction.CREATE, requestData);
        return get().insertReturningPrimary(validateOnCreate(parse(reqData.body()), reqData.headers()))
                    .flatMap(k -> cudResponse(EventAction.CREATE, k, reqData));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return doUpdate(requestData, EventAction.UPDATE, this::validateOnUpdate);
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return doUpdate(requestData, EventAction.PATCH, this::validateOnPatch);
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        RequestData reqData = recompute(EventAction.REMOVE, requestData);
        final K pk = parsePrimaryKey(reqData);
        return doGetOne(reqData).flatMap(m -> get().deleteById(pk)
                                                   .filter(r -> r > 0)
                                                   .switchIfEmpty(Single.error(notFound(pk)))
                                                   .flatMap(r -> enableFullResourceInCUDResponse()
                                                                 ? cudResponse(EventAction.REMOVE, m, reqData)
                                                                 : cudResponse(EventAction.REMOVE, pk, reqData)));
    }

}
