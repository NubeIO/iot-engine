package com.nubeiot.core.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractModelService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    implements EventListener {

    @Getter(value = AccessLevel.PROTECTED)
    private final D dao;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.UPDATE, EventAction.PATCH, EventAction.REMOVE,
                             EventAction.GET_ONE, EventAction.GET_LIST);
    }

    protected abstract M parse(@NonNull JsonObject object);

    @NonNull
    protected abstract Table<R> table();

    protected abstract K id(String requestKey) throws IllegalArgumentException;

    protected abstract boolean hasTimeAudit();

    @NonNull
    protected abstract String listKey();

    protected M validateOnCreate(M pojo) throws IllegalArgumentException {
        return pojo;
    }

    protected M validateOnUpdate(M pojo) throws IllegalArgumentException {
        return pojo;
    }

    protected M validateOnPatch(M pojo) throws IllegalArgumentException {
        return pojo;
    }

    @NonNull
    protected String idKey() {
        return "id";
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
        final K id = id(requestData.body().getString(idKey()));
        return dao.findOneById(id)
                  .map(o -> o.orElseThrow(() -> new NotFoundException(Strings.format("Not found id '{0}'", id))))
                  .map(VertxPojo::toJson);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(@Param("data") M pojo) {
        return dao.insertReturningPrimary(validateOnCreate(pojo)).map(k -> new JsonObject().put(idKey(), k.toString()));
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return dao.update(validateOnUpdate(parse(requestData.body())))
                  .map(k -> new JsonObject().put(idKey(), k.toString()));
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return dao.update(validateOnPatch(parse(requestData.body())))
                  .map(k -> new JsonObject().put(idKey(), k.toString()));
    }

    private ResultQuery<R> filter(JsonObject filter, Pagination pagination, DSLContext context) {
        SelectConditionStep<R> sql = context.selectFrom(table()).where(DSL.trueCondition());
        return filter(filter, sql).limit(pagination.getPerPage())
                                  .offset((pagination.getPage() - 1) * pagination.getPerPage());
    }

    protected SelectConditionStep<R> filter(JsonObject filter, SelectConditionStep<R> sql) {
        Set<String> fieldNames = Arrays.stream(table().fields()).map(Field::getName).collect(Collectors.toSet());
        filter.stream()
              .parallel()
              .filter(entry -> fieldNames.contains(entry.getKey()))
              .forEach(entry -> sql.and(((Field) table().field(entry.getKey())).eq(entry.getValue())));
        return sql;
    }

}
