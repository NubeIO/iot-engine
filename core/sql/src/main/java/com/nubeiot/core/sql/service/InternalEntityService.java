package com.nubeiot.core.sql.service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOptionStep;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function3;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;

interface InternalEntityService<M extends EntityMetadata, V extends EntityValidation> extends EntityService<M, V> {

    /**
     * Do recompute request data
     */
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        return requestData;
    }

    /**
     * Do get list entity resources
     *
     * @param requestData Request data
     * @return list pojo entities
     */
    default Observable<VertxPojo> doGetList(RequestData requestData) {
        return dao().queryExecutor().findMany(ctx -> query(ctx, requestData)).flattenAsObservable(records -> records);
    }

    @SuppressWarnings("unchecked")
    default @NonNull <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> D dao() {
        return (D) metadata().getDao(entityHandler());
    }

    /**
     * Do get one resource by {@code primary key} or by {@code rich query} after analyzing given request data
     *
     * @param requestData Request data
     * @return single pojo
     */
    default Single<VertxPojo> doGetOne(RequestData requestData) {
        Object pk = metadata().parsePrimaryKey(requestData);
        return dao().findOneById(pk).map(o -> o.orElseThrow(() -> metadata().notFound(pk)));
    }

    /**
     * Do update data on both {@code UPDATE} or {@code PATCH} action
     *
     * @param requestData Request data
     * @param action      Event action
     * @param validation  Validation function
     * @return single response in {@code json}
     * @see #cudResponse(EventAction, VertxPojo, RequestData)
     */
    default Single<JsonObject> doUpdate(RequestData requestData, EventAction action,
                                        Function3<VertxPojo, VertxPojo, JsonObject, VertxPojo> validation) {
        RequestData reqData = recompute(action, requestData);
        final Object pk = metadata().parsePrimaryKey(reqData);
        return doGetOne(reqData).map(
            db -> validation.apply(db, metadata().parse(reqData.body().put(metadata().jsonKeyName(), pk)),
                                   reqData.headers()))
                                .flatMap(dao()::update)
                                .filter(i -> i > 0)
                                .switchIfEmpty(Single.error(metadata().notFound(pk)))
                                .flatMap(i -> cudResponse(action, pk, reqData));
    }

    /**
     * Do query data
     *
     * @param ctx         DSL Context
     * @param requestData Request data
     * @return result query
     * @see #filter(SelectConditionStep, JsonObject)
     * @see #paging(SelectConditionStep, Pagination)
     */
    @SuppressWarnings("unchecked")
    default <R extends UpdatableRecord<R>> ResultQuery<R> query(@NonNull DSLContext ctx,
                                                                @NonNull RequestData requestData) {
        return paging(filter(ctx.selectFrom(metadata().table()).where(DSL.trueCondition()), requestData.getFilter()),
                      requestData.getPagination());
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
    default <R extends UpdatableRecord<R>> SelectConditionStep<R> filter(@NonNull SelectConditionStep<R> sql,
                                                                         JsonObject filter) {
        if (Objects.isNull(filter)) {
            return sql;
        }
        final Map<String, String> jsonFields = metadata().table().jsonFields();
        filter.stream().map(entry -> {
            final Field field = metadata().table().field(jsonFields.getOrDefault(entry.getKey(), entry.getKey()));
            return Optional.ofNullable(entry.getValue()).map(field::eq).orElseGet(field::isNull);
        }).forEach(sql::and);
        return sql;
    }

    /**
     * Do query paging
     *
     * @param sql        SQL select command
     * @param pagination Given pagination
     * @return Database Select DSL
     */
    default <R extends UpdatableRecord<R>> SelectOptionStep<R> paging(@NonNull SelectConditionStep<R> sql,
                                                                      Pagination pagination) {
        Pagination paging = Optional.ofNullable(pagination).orElseGet(() -> Pagination.builder().build());
        return sql.limit(paging.getPerPage()).offset((paging.getPage() - 1) * paging.getPerPage());
    }

    /**
     * Get one resource by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return one single data source if found else throw {@code not found exception}
     * @see EntityMetadata#notFound(Object)
     */
    default Single<? extends VertxPojo> lookupById(@NonNull Object primaryKey) {
        return dao().findOneById(primaryKey).map(o -> o.orElseThrow(() -> metadata().notFound(primaryKey)));
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
        return transformer().enableFullResourceInCUDResponse()
               ? lookupById(key).flatMap(r -> cudResponse(action, r, requestData))
               : Single.just(new JsonObject().put(metadata().requestKeyName(), JsonData.checkAndConvert(key)));
    }

}
