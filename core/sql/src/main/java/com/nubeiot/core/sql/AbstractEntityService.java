package com.nubeiot.core.sql;

import java.util.Objects;
import java.util.Optional;
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
import io.reactivex.Single;
import io.reactivex.functions.Function3;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.sql.type.TimeAudit;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for entity
 */
public abstract class AbstractEntityService<K, M extends VertxPojo, R extends UpdatableRecord<R>,
                                               D extends VertxDAO<R, M, K>>
    implements EntityService<K, M, R, D> {

    private final Supplier<D> dao;

    public AbstractEntityService(@NonNull EntityHandler entityHandler) {
        this.dao = () -> entityHandler.getDao(daoClass());
    }

    /**
     * Defines enabling {@code time audit} in {@code application layer} instead of {@code database layer} by {@code DB
     * trigger}. It is helpful to add time audit in {@code create/update/patch} resource.
     *
     * @return {@code true} if enable time audit in application layer
     * @see TimeAudit
     */
    protected abstract boolean enableTimeAudit();

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
     * Defines listener for listing Resource
     *
     * @param requestData Request data
     * @return Json object includes list data
     * @see EventAction#GET_LIST
     */
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        return get().queryExecutor()
                    .findMany(ctx -> query(ctx, requestData))
                    .flattenAsObservable(records -> records)
                    .flatMapSingle(m -> Single.just(customizeEachItem(m)))
                    .collect(JsonArray::new, JsonArray::add)
                    .map(results -> new JsonObject().put(listKey(), results));
    }

    /**
     * Defines listener for get one item by key
     *
     * @param requestData Request data
     * @return Json object represents resource data
     * @see EventAction#GET_ONE
     */
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        return lookupById(parsePK(requestData)).map(this::customizeItem);
    }

    /**
     * Defines listener for creating new resource
     *
     * @param requestData Request data
     * @return json object that includes primary key
     * @see EventAction#CREATE
     */
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return get().insertReturningPrimary(validateOnCreate(parse(requestData.body()), requestData.headers()))
                    .map(k -> new JsonObject().put(primaryKeyName(), k.toString()));
    }

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#UPDATE
     */
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return update(requestData, this::validateOnUpdate);
    }

    /**
     * Defines listener for patching existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#PATCH
     */
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return update(requestData, this::validateOnPatch);
    }

    /**
     * Defines listener for deleting existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#REMOVE
     */
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        final K pk = parsePK(requestData);
        return get().deleteById(pk)
                    .map(i -> Optional.ofNullable(i > 0 ? true : null))
                    .map(r -> new JsonObject().put("success", r.orElseThrow(() -> new NotFoundException(
                        Strings.format("Not found resource with {0}={1}", primaryKeyName(), pk)))));
    }

    /**
     * Defines primary key name in json to lookup in {@code get/update/patch/delete} resource
     *
     * @return primary key name
     */
    @NonNull
    protected String primaryKeyName() {
        return "id";
    }

    /**
     * Do any transform or convert each resource item in {@link #list(RequestData)}
     *
     * @param pojo item
     * @return transformer item
     */
    @NonNull
    protected JsonObject customizeEachItem(@NonNull M pojo) {
        return pojo.toJson();
    }

    /**
     * Do any transform or convert resource item in {@link #get(RequestData)}
     *
     * @param pojo item
     * @return transformer item
     */
    @NonNull
    protected JsonObject customizeItem(@NonNull M pojo) {
        return pojo.toJson();
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
        Pagination pagination = Objects.isNull(requestData.getPagination())
                                ? Pagination.builder().build()
                                : requestData.getPagination();
        return paging(filter(ctx.selectFrom(table()).where(DSL.trueCondition()), requestData.getFilter()), pagination);
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
    @SuppressWarnings("unchecked")
    protected SelectConditionStep<R> filter(@NonNull SelectConditionStep<R> sql, JsonObject filter) {
        if (Objects.isNull(filter)) {
            return sql;
        }
        filter.stream()
              .filter(f -> table().jsonFields().containsKey(f.getKey()))
              .forEach(f -> sql.and(((Field) table().field(table().jsonFields().get(f.getKey()))).eq(f.getValue())));
        return sql;
    }

    /**
     * Do query paging
     *
     * @param sql        SQL select command
     * @param pagination pag
     * @return Database Select DSL
     */
    protected SelectOptionStep<R> paging(@NonNull SelectConditionStep<R> sql, @NonNull Pagination pagination) {
        return sql.limit(pagination.getPerPage()).offset((pagination.getPage() - 1) * pagination.getPerPage());
    }

    private K parsePK(RequestData requestData) {
        return parsePK(requestData.body().getValue(primaryKeyName()).toString());
    }

    private Single<M> lookupById(@NonNull K pk) {
        return get().findOneById(pk)
                    .map(o -> o.orElseThrow(() -> new NotFoundException(
                        Strings.format("Not found resource with {0}={1}", primaryKeyName(), pk))));
    }

    private Single<JsonObject> update(RequestData requestData, Function3<M, M, JsonObject, M> validate) {
        M req = parse(requestData.body());
        JsonObject headers = requestData.headers();
        return lookupById(parsePK(requestData)).map(db -> validate.apply(db, req, headers))
                                               .flatMap(get()::update)
                                               .map(i -> i > 0)
                                               .map(r -> new JsonObject().put("success", r));
    }

    private M addModifiedAudit(M dbData, @NonNull M pojo, JsonObject headers) {
        if (pojo instanceof HasTimeAudit && enableTimeAudit()) {
            ((HasTimeAudit) pojo).setTimeAudit(
                TimeAudit.modified(((HasTimeAudit) dbData).getTimeAudit(), headers.getString(Headers.X_REQUEST_USER)));
        }
        return pojo;
    }

}
