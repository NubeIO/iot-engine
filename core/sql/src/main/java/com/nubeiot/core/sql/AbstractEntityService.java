package com.nubeiot.core.sql;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.sql.type.SyncAudit;
import com.nubeiot.core.sql.type.TimeAudit;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for entity
 */
public abstract class AbstractEntityService<K, M extends VertxPojo, R extends UpdatableRecord<R>,
                                               D extends VertxDAO<R, M, K>>
    implements EntityService<K, M, R, D> {

    public static final Set<String> IGNORE_FIELDS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("time_audit", "sync_audit")));
    @Getter(value = AccessLevel.PROTECTED)
    private final EntityHandler entityHandler;
    private final Supplier<D> dao;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractEntityService(@NonNull EntityHandler entityHandler) {
        this.entityHandler = entityHandler;
        this.dao = () -> entityHandler.getDao(daoClass());
    }

    /**
     * Defines key name in respond data in {@code list} resource
     *
     * @return key name
     * @see #list(RequestData)
     */
    @NonNull
    protected abstract String listKey();

    @Override
    public D get() {
        return dao.get();
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_LIST, requestData);
        return doGetList(reqData).flatMapSingle(m -> Single.just(customizeEachItem(m, reqData)))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put(listKey(), results));
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
        final K pk = parsePK(reqData);
        return doGetOne(reqData).flatMap(m -> get().deleteById(pk)
                                                   .filter(r -> r > 0)
                                                   .switchIfEmpty(Single.error(notFound(pk)))
                                                   .flatMap(r -> enableFullResourceInCUDResponse()
                                                                 ? cudResponse(EventAction.REMOVE, m, reqData)
                                                                 : cudResponse(EventAction.REMOVE, pk, reqData)));
    }

    /**
     * Do recompute request data
     */
    @NonNull
    protected RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        return requestData;
    }

    /**
     * Do any transform or convert each resource item in {@link #list(RequestData)}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     */
    @NonNull
    protected JsonObject customizeEachItem(@NonNull M pojo, @NonNull RequestData requestData) {
        return customizeGetItem(pojo, requestData);
    }

    /**
     * Do any transform or convert resource item in {@link #get(RequestData)}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     */
    @NonNull
    protected JsonObject customizeGetItem(@NonNull M pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(IGNORE_FIELDS);
    }

    /**
     * Do any transform or convert in {@code CUD response} if {@link #enableFullResourceInCUDResponse()} is {@code
     * true}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     */
    @NonNull
    protected JsonObject customizeModifiedItem(@NonNull M pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(JsonData.MAPPER, IGNORE_FIELDS);
    }

    protected Observable<M> doGetList(RequestData requestData) {
        return get().queryExecutor().findMany(ctx -> query(ctx, requestData)).flattenAsObservable(records -> records);
    }

    protected Single<M> doGetOne(RequestData requestData) {
        K pk = parsePrimaryKey(requestData);
        return get().findOneById(pk).map(o -> o.orElseThrow(() -> notFound(pk)));
    }

    protected Single<JsonObject> doUpdate(RequestData requestData, EventAction action,
                                          Function3<M, M, JsonObject, M> validate) {
        RequestData reqData = recompute(action, requestData);
        final K pk = parsePK(reqData);
        M req = parse(reqData.body().put(jsonKeyName(), pk));
        JsonObject headers = reqData.headers();
        return doGetOne(reqData).map(db -> validate.apply(db, req, headers))
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
    protected M validateOnCreate(@NonNull M pojo, @NonNull JsonObject headers) throws IllegalArgumentException {
        if (pojo instanceof HasTimeAudit && enableTimeAudit()) {
            ((HasTimeAudit) pojo).setTimeAudit(TimeAudit.created(headers.getString(Headers.X_REQUEST_USER)));
        }
        if (pojo instanceof HasSyncAudit) {
            ((HasSyncAudit) pojo).setSyncAudit(SyncAudit.notSynced());
        }
        return pojo;
    }

    /**
     * Validate when updating resource. Any {@code override} method for custom validation by each object should be ended
     * by recall {@code super} to implement {@code time audit}
     *
     * @param dbData  existing resource object from database
     * @param pojo    given request resource object
     * @param headers request header. It is useful to audit
     * @return instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    protected M validateOnUpdate(@NonNull M dbData, @NonNull M pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return addModifiedAudit(dbData, pojo, headers);
    }

    /**
     * Validate when patching resource. Any {@code override} method for custom validation by each object should be ended
     * by recall {@code super} to implement {@code time audit}
     *
     * @param dbData  existing resource object from database
     * @param pojo    given request resource object
     * @param headers request header. It is useful to audit
     * @return instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    protected M validateOnPatch(@NonNull M dbData, @NonNull M pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return addModifiedAudit(dbData, parse(JsonPojo.merge(dbData, pojo)), headers);
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
    protected ResultQuery<R> query(@NonNull DSLContext ctx, @NonNull RequestData requestData) {
        return paging(filter(ctx.selectFrom(table()).where(DSL.trueCondition()), requestData.getFilter()),
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
    //TODO It depends on RQL in future https://github.com/NubeIO/iot-engine/issues/128
    @SuppressWarnings("unchecked")
    protected SelectConditionStep<R> filter(@NonNull SelectConditionStep<R> sql, JsonObject filter) {
        if (Objects.isNull(filter)) {
            return sql;
        }
        final Map<String, String> jsonFields = table().jsonFields();
        filter.stream()
              .forEach(f -> sql.and(
                  ((Field) table().field(jsonFields.getOrDefault(f.getKey(), f.getKey()))).eq(f.getValue())));
        return sql;
    }

    /**
     * Do query paging
     *
     * @param sql        SQL select command
     * @param pagination pag
     * @return Database Select DSL
     */
    protected SelectOptionStep<R> paging(@NonNull SelectConditionStep<R> sql, Pagination pagination) {
        Pagination paging = Optional.ofNullable(pagination).orElseGet(() -> Pagination.builder().build());
        return sql.limit(paging.getPerPage()).offset((paging.getPage() - 1) * paging.getPerPage());
    }

    /**
     * Construct {@code CUD Response} that includes full resource
     *
     * @param action      Event action
     * @param pojo        Pojo data
     * @param requestData request data
     * @return response
     */
    protected Single<JsonObject> cudResponse(@NonNull EventAction action, @NonNull M pojo,
                                             @NonNull RequestData requestData) {
        return Single.just(new JsonObject().put("resource", customizeModifiedItem(pojo, requestData))
                                           .put("action", action)
                                           .put("status", Status.SUCCESS));
    }

    private Single<M> lookupById(@NonNull K pk) {
        return get().findOneById(pk).map(o -> o.orElseThrow(() -> notFound(pk)));
    }

    private Single<JsonObject> cudResponse(@NonNull EventAction action, @NonNull K k,
                                           @NonNull RequestData requestData) {
        return enableFullResourceInCUDResponse()
               ? lookupById(k).flatMap(r -> cudResponse(action, r, requestData))
               : Single.just(new JsonObject().put(primaryKeyName(),
                                                  ReflectionClass.isJavaLangObject(k.getClass()) ? k : k.toString()));
    }

    protected NotFoundException notFound(K pk) {
        return new NotFoundException(Strings.format("Not found resource with {0}={1}", primaryKeyName(), pk));
    }

    private K parsePK(RequestData requestData) {
        return parsePrimaryKey(requestData.body().getValue(primaryKeyName()).toString());
    }

    private M addModifiedAudit(M dbData, @NonNull M pojo, JsonObject headers) {
        if (pojo instanceof HasTimeAudit && enableTimeAudit()) {
            ((HasTimeAudit) pojo).setTimeAudit(
                TimeAudit.modified(((HasTimeAudit) dbData).getTimeAudit(), headers.getString(Headers.X_REQUEST_USER)));
        }
        if (pojo instanceof HasSyncAudit) {
            final SyncAudit syncAudit = Optional.ofNullable(((HasSyncAudit) pojo).getSyncAudit())
                                                .orElseGet(SyncAudit::notSynced);
            ((HasSyncAudit) pojo).setSyncAudit(syncAudit);
        }
        return pojo;
    }

}
