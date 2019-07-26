package com.nubeiot.core.sql;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractModelService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    implements ModelService<K, M, R, D> {

    @Getter
    private final D dao;

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
        return addModifiedAudit(dbData, pojo, headers);
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        return dao.queryExecutor()
                  .findMany(dslContext -> filter(requestData.getFilter(), requestData.getPagination(), dslContext))
                  .flattenAsObservable(records -> records)
                  .flatMapSingle(m -> Single.just(m.toJson()))
                  .collect(JsonArray::new, JsonArray::add)
                  .map(results -> new JsonObject().put(listKey(), results));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        return lookupById(requestData.body().getString(primaryKeyName())).map(VertxPojo::toJson);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return dao.insertReturningPrimary(validateOnCreate(parse(requestData.body()), requestData.headers()))
                  .map(k -> new JsonObject().put(primaryKeyName(), k.toString()));
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return update(requestData, this::validateOnUpdate);
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return update(requestData, this::validateOnPatch);
    }

    private ResultQuery<R> filter(JsonObject filter, Pagination pagination, DSLContext context) {
        SelectConditionStep<R> sql = context.selectFrom(table()).where(DSL.trueCondition());
        return filter(filter, sql).limit(pagination.getPerPage())
                                  .offset((pagination.getPage() - 1) * pagination.getPerPage());
    }

    @SuppressWarnings("unchecked")
    protected SelectConditionStep<R> filter(JsonObject filter, SelectConditionStep<R> sql) {
        filter.stream()
              .filter(f -> table().jsonFields().containsKey(f.getKey()))
              .forEach(f -> sql.and(((Field) table().field(table().jsonFields().get(f.getKey()))).eq(f.getValue())));
        return sql;
    }

    private Single<M> lookupById(String requestPK) {
        final K id = parsePK(requestPK);
        return dao.findOneById(id)
                  .map(o -> o.orElseThrow(() -> new NotFoundException(
                      Strings.format("Not found resource with {0}='{1}'", primaryKeyName(), id))));
    }

    private Single<JsonObject> update(RequestData requestData, Function3<M, M, JsonObject, M> validate) {
        M req = parse(requestData.body());
        JsonObject headers = requestData.headers();
        return lookupById(requestData.body().getString(primaryKeyName())).map(db -> validate.apply(db, req, headers))
                                                                         .flatMap(dao::update)
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
