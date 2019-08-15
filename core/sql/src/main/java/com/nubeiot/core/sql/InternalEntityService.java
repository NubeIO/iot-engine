package com.nubeiot.core.sql;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

interface InternalEntityService<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
                                   M extends EntityMetadata<K, P, R, D>>
    extends EntityService<K, P, R, D, M> {

    /**
     * Represents set of audit fields. Use {@link Filters#AUDIT} to expose these fields
     */
    Set<String> AUDIT_FIELDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("time_audit", "sync_audit")));

    /**
     * Do recompute request data
     */
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        return requestData;
    }

    /**
     * Get ignore fields by request data. By default, it excludes {@link #AUDIT_FIELDS}.
     * <p>
     * If {@code request data} has {@link Filters#AUDIT}, it will expose these {@code audit fields}
     *
     * @param requestData Request data
     * @return set of ignore fields when do customizing response data
     * @see #customizeCreatedItem(VertxPojo, RequestData)
     * @see #customizeDeletedItem(VertxPojo, RequestData)
     * @see #customizeModifiedItem(VertxPojo, RequestData)
     * @see #customizeGetItem(VertxPojo, RequestData)
     * @see #customizeEachItem(VertxPojo, RequestData)
     */
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        return requestData.hasAudit() ? Collections.emptySet() : AUDIT_FIELDS;
    }

    /**
     * Do any transform or convert each resource item in {@link #list(RequestData)}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result omits {@code null} fields
     */
    @NonNull
    default JsonObject customizeEachItem(@NonNull P pojo, @NonNull RequestData requestData) {
        return customizeGetItem(pojo, requestData);
    }

    /**
     * Do any transform or convert resource item in {@link #get(RequestData)}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result omits {@code null} fields
     */
    @NonNull
    default JsonObject customizeGetItem(@NonNull P pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(ignoreFields(requestData));
    }

    /**
     * Do any transform resource after {@code CREATE} action successfully if {@link #enableFullResourceInCUDResponse()}
     * is {@code true}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result doesn't omits {@code null} fields
     */
    @NonNull
    default JsonObject customizeCreatedItem(@NonNull P pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(JsonData.MAPPER, ignoreFields(requestData));
    }

    /**
     * Do any transform resource after {@code UPDATE} or {@code PATCH} action successfully if {@link
     * #enableFullResourceInCUDResponse()} is {@code true}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result omits {@code null} fields and {@link #AUDIT_FIELDS}
     */
    @NonNull
    default JsonObject customizeModifiedItem(@NonNull P pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(ignoreFields(requestData));
    }

    /**
     * Do any transform resource after {@code DELETE} action successfully if {@link #enableFullResourceInCUDResponse()}
     * is {@code true}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result doesn't omits {@code null} fields
     */
    @NonNull
    default JsonObject customizeDeletedItem(@NonNull P pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(JsonData.MAPPER, ignoreFields(requestData));
    }

    /**
     * Do get list entity resources
     *
     * @param requestData Request data
     * @return list pojo entities
     */
    default Observable<? extends P> doGetList(RequestData requestData) {
        return get().queryExecutor().findMany(ctx -> query(ctx, requestData)).flattenAsObservable(records -> records);
    }

    /**
     * Do get one resource by {@code primary key} or by {@code rich query} after analyzing given request data
     *
     * @param requestData Request data
     * @return single pojo
     */
    default Single<? extends P> doGetOne(RequestData requestData) {
        K pk = parsePrimaryKey(requestData);
        return get().findOneById(pk).map(o -> o.orElseThrow(() -> notFound(pk)));
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
                                        Function3<P, P, JsonObject, P> validation) {
        RequestData reqData = recompute(action, requestData);
        final K pk = parsePrimaryKey(reqData);
        return doGetOne(reqData).map(
            db -> validation.apply(db, parse(reqData.body().put(metadata().jsonKeyName(), pk)), reqData.headers()))
                                .flatMap(get()::update)
                                .filter(i -> i > 0)
                                .switchIfEmpty(Single.error(notFound(pk)))
                                .flatMap(i -> cudResponse(action, pk, reqData));
    }

    /**
     * Validate when creating new resource. Any {@code override} method for custom validation by each object should be
     * ended by recall {@code super} to implement {@code time audit}
     *
     * @param pojo    given request resource object
     * @param headers request header. It is useful to audit
     * @return instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    default P validateOnCreate(@NonNull P pojo, @NonNull JsonObject headers) throws IllegalArgumentException {
        return EntityAuditHandler.addCreationAudit(enableTimeAudit(), pojo,
                                                   headers.getString(Headers.X_REQUEST_USER, null));
    }

    /**
     * Validate when updating resource. Any {@code override} method for custom validation by each object should be ended
     * by recall {@code super} to implement {@code time audit}
     *
     * @param dbData  existing resource object from database
     * @param pojo    given request resource object
     * @param headers request header. It is useful to audit
     * @return pojo instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    default P validateOnUpdate(@NonNull P dbData, @NonNull P pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return EntityAuditHandler.addModifiedAudit(enableTimeAudit(), dbData, pojo,
                                                   headers.getString(Headers.X_REQUEST_USER, null));
    }

    /**
     * Validate when patching resource. Any {@code override} method for custom validation by each object should be ended
     * by recall {@code super} to implement {@code time audit}
     *
     * @param dbData  existing resource object from database
     * @param pojo    given request resource object
     * @param headers request header. It is useful to audit
     * @return pojo instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    default P validateOnPatch(@NonNull P dbData, @NonNull P pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return EntityAuditHandler.addModifiedAudit(enableTimeAudit(), dbData, parse(JsonPojo.merge(dbData, pojo)),
                                                   headers.getString(Headers.X_REQUEST_USER, null));
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
    default ResultQuery<R> query(@NonNull DSLContext ctx, @NonNull RequestData requestData) {
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
    default SelectConditionStep<R> filter(@NonNull SelectConditionStep<R> sql, JsonObject filter) {
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
    default SelectOptionStep<R> paging(@NonNull SelectConditionStep<R> sql, Pagination pagination) {
        Pagination paging = Optional.ofNullable(pagination).orElseGet(() -> Pagination.builder().build());
        return sql.limit(paging.getPerPage()).offset((paging.getPage() - 1) * paging.getPerPage());
    }

    /**
     * Get one resource by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return one single data source if found else throw {@code not found exception}
     * @see #notFound(K)
     */
    default Single<P> lookupById(@NonNull K primaryKey) {
        return get().findOneById(primaryKey).map(o -> o.orElseThrow(() -> notFound(primaryKey)));
    }

    /**
     * Construct {@code CUD Response} that includes full resource
     *
     * @param action      Event action
     * @param pojo        Pojo data
     * @param requestData request data
     * @return response
     */
    default Single<JsonObject> cudResponse(@NonNull EventAction action, @NonNull P pojo,
                                           @NonNull RequestData requestData) {
        JsonObject result = action == EventAction.CREATE
                            ? customizeCreatedItem(pojo, requestData)
                            : action == EventAction.REMOVE
                              ? customizeDeletedItem(pojo, requestData)
                              : customizeModifiedItem(pojo, requestData);
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
    default Single<JsonObject> cudResponse(@NonNull EventAction action, @NonNull K key,
                                           @NonNull RequestData requestData) {
        return enableFullResourceInCUDResponse()
               ? lookupById(key).flatMap(r -> cudResponse(action, r, requestData))
               : Single.just(new JsonObject().put(metadata().requestKeyName(),
                                                  ReflectionClass.isJavaLangObject(key.getClass())
                                                  ? key
                                                  : key.toString()));
    }

    /**
     * Construct {@code NotFound exception} by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return NotFoundException
     */
    default NotFoundException notFound(K primaryKey) {
        return new NotFoundException(
            Strings.format("Not found resource with {0}={1}", metadata().requestKeyName(), primaryKey));
    }

}
