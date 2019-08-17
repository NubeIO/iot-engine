package com.nubeiot.core.sql.service;

import org.jooq.SelectConditionStep;
import org.jooq.SelectOptionStep;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;

interface InternalEntityService<M extends EntityMetadata, V extends EntityValidation> extends EntityService<M, V> {

    @Override
    @NonNull SimpleQueryExecutor queryExecutor();

    /**
     * Do recompute request data
     */
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        return requestData;
    }

    @SuppressWarnings("unchecked")
    default @NonNull <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> D dao() {
        return (D) metadata().dao(entityHandler());
    }

    /**
     * Do query filter
     * <p>
     * It is simple filter function by equal comparision. Any complex query should be override by each service.
     *
     * @param sql    Select condition step command
     * @param filter Filter request
     * @return Database Select DSL
     * @see SelectConditionStep
     */
    //TODO Rich query depends on RQL in future https://github.com/NubeIO/iot-engine/issues/128
    @SuppressWarnings("unchecked")
    //TODO REMOVE
    default <R extends UpdatableRecord<R>> SelectConditionStep<R> filter(@NonNull SelectConditionStep<R> sql,
                                                                         JsonObject filter) {
        return queryExecutor().filter(sql.configuration().dsl(), metadata().table(), filter);
    }

    /**
     * Do query paging
     *
     * @param sql        SQL select command
     * @param pagination Given pagination
     * @return Database Select DSL
     */
    @SuppressWarnings("unchecked")
    default <R extends UpdatableRecord<R>> SelectOptionStep<R> paging(@NonNull SelectConditionStep<R> sql,
                                                                      Pagination pagination) {
        return (SelectOptionStep<R>) queryExecutor().paging(sql, pagination);
    }

    /**
     * Get one resource by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return one single data source if found else throw {@code not found exception}
     * @see EntityMetadata#notFound(Object)
     */
    default Maybe<? extends VertxPojo> lookupById(@NonNull Object primaryKey) {
        return queryExecutor().lookupById(primaryKey);
    }

    /**
     * Construct {@code CUD Response} that includes full resource
     *
     * @param action      Event action
     * @param pojo        Pojo data
     * @param requestData request data
     * @return response
     */
    default <P extends VertxPojo> Single<JsonObject> cudResponse(@NonNull EventAction action, @NonNull P pojo,
                                                                 @NonNull RequestData requestData) {
        JsonObject result = action == EventAction.CREATE
                            ? transformer().afterCreate(pojo, requestData)
                            : action == EventAction.REMOVE
                              ? transformer().afterDelete(pojo, requestData)
                              : transformer().afterModify(pojo, requestData);
        return Single.just(
            new JsonObject().put("resource", result).put("action", action).put("status", Status.SUCCESS));
    }

    /**
     * Construct {@code CUD Response} that includes full resource
     *
     * @param action      Event action
     * @param key         Given primary key
     * @param requestData request data
     * @return response
     */
    default Single<JsonObject> cudResponse(@NonNull EventAction action, @NonNull Object key,
                                           @NonNull RequestData requestData) {
        return transformer().enableFullResourceInCUDResponse() ? lookupById(key).flatMapSingle(
            r -> cudResponse(action, r, requestData))
                                                               : Single.just(new JsonObject().put(metadata().requestKeyName(), JsonData.checkAndConvert(key)));
    }

}
