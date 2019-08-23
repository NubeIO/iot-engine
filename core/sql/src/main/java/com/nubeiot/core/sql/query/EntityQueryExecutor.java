package com.nubeiot.core.sql.query;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOptionStep;
import org.jooq.Table;
import org.jooq.exception.TooManyRowsException;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.core.sql.tables.JsonTable;

import lombok.NonNull;

public interface EntityQueryExecutor<P extends VertxPojo> {

    static Single wrapDatabaseError(Throwable throwable) {
        if (throwable instanceof TooManyRowsException) {
            return Single.error(new ImplementationError(ErrorCode.DATABASE_ERROR,
                                                        "Query is not correct, the result contains more than one " +
                                                        "record", throwable));
        }
        return Single.error(throwable);
    }

    static Single unableDelete(String clue) {
        return Single.error(
            new NubeException("Cannot delete record", new HiddenException(ErrorCode.DATABASE_ERROR, clue)));
    }

    EntityHandler entityHandler();

    /**
     * Find many entity resources
     *
     * @param requestData Request data
     * @return list pojo entities
     */
    Observable<P> findMany(RequestData requestData);

    /**
     * Find one resource by {@code primary key} or by {@code composite unique key} after analyzing given request data
     *
     * @param requestData Request data
     * @return single pojo
     */
    Single<P> findOneByKey(RequestData requestData);

    /**
     * Get one resource by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return one single data source if found else throw {@code not found exception}
     * @see EntityMetadata#notFound(Object)
     */
    Maybe<P> lookupByPrimaryKey(@NonNull Object primaryKey);

    /**
     * Create new resource then return {@code primary key}
     *
     * @param pojo        new resource
     * @param requestData request data
     * @return primary key
     */
    Single<?> insertReturningPrimary(@NonNull P pojo, @NonNull RequestData requestData);

    /**
     * Do update data on both {@code UPDATE} or {@code PATCH} action
     *
     * @param requestData Request data
     * @param action      Event action
     * @param validator   Compare and convert function between {@code database resource} and {@code requested resource}
     * @return primary key
     */
    Single<?> modifyReturningPrimary(@NonNull RequestData requestData, @NonNull EventAction action,
                                     BiFunction<VertxPojo, RequestData, VertxPojo> validator);

    /**
     * Do delete data by primary
     *
     * @param requestData Request data
     * @return deleted resource
     */
    Single<P> deleteOneByKey(RequestData requestData);

    /**
     * Create view query
     *
     * @param filter     Request filter
     * @param pagination pagination
     * @return query function
     */
    Function<DSLContext, ? extends ResultQuery<? extends Record>> viewQuery(JsonObject filter, Pagination pagination);

    /**
     * Create view query for one resource
     *
     * @param filter Request filter
     * @return query function
     */
    default Function<DSLContext, ? extends ResultQuery<? extends Record>> viewOneQuery(JsonObject filter) {
        return viewQuery(filter, Pagination.oneValue());
    }

    default Function<DSLContext, Boolean> fetchExists(@NonNull Table table, @NonNull Condition condition) {
        return dsl -> dsl.fetchExists(table, condition);
    }

    default Function<DSLContext, Boolean> fetchExists(@NonNull EntityMetadata metadata, @NonNull Object key) {
        return fetchExists(metadata.table(), conditionByPrimary(metadata, key));
    }

    @SuppressWarnings("unchecked")
    default Single<P> isAbleToDelete(@NonNull P pojo, @NonNull EntityMetadata metadata,
                                     Function<VertxPojo, String> keyProvider) {
        if (!(entityHandler() instanceof EntityConstraintHolder)) {
            return Single.just(pojo);
        }
        final Object pk = pojo.toJson().getValue(metadata.jsonKeyName());
        final EntityConstraintHolder holder = (EntityConstraintHolder) entityHandler();
        return Observable.fromIterable(holder.referenceTableKeysTo(metadata.table()))
                         .flatMapSingle(e -> entityHandler().genericQuery()
                                                            .executeAny(fetchExists(e.getKey(), e.getValue().eq(pk)))
                                                            .filter(exist -> !exist)
                                                            .switchIfEmpty(Single.error(
                                                                metadata.unableDeleteDueUsing(keyProvider.apply(pojo))))
                                                            .map(b -> pojo))
                         .switchIfEmpty(Observable.just(pojo))
                         .singleOrError();
    }

    /**
     * Create database condition by request filter
     * <p>
     * It is simple filter function by equal comparision. Any complex query should be override by each service.
     *
     * @param table  Resource table
     * @param filter Filter request
     * @return Database Select DSL
     * @see Condition
     */
    //TODO Rich query depends on RQL in future https://github.com/NubeIO/iot-engine/issues/128
    default Condition condition(@NonNull JsonTable<? extends Record> table, JsonObject filter) {
        return condition(table, filter, false);
    }

    default Condition condition(@NonNull JsonTable<? extends Record> table, JsonObject filter, boolean allowNullable) {
        if (Objects.isNull(filter)) {
            return DSL.trueCondition();
        }
        Condition[] c = new Condition[] {DSL.trueCondition()};
        filter.stream().map(entry -> {
            final Field field = table.getField(entry.getKey());
            return Optional.ofNullable(field)
                           .map(f -> Optional.ofNullable(entry.getValue())
                                             .map(v -> allowNullable ? f.eq(v).or(f.isNull()) : f.eq(v))
                                             .orElseGet(f::isNull))
                           .orElse(null);
        }).filter(Objects::nonNull).forEach(condition -> c[0] = c[0].and(condition));
        return c[0];
    }

    @SuppressWarnings("unchecked")
    default Condition conditionByPrimary(@NonNull EntityMetadata metadata, @NonNull Object key) {
        return metadata.table().getField(metadata.jsonKeyName()).eq(key);
    }

    /**
     * Do query paging
     *
     * @param sql        SQL select command
     * @param pagination Given pagination
     * @return Database Select DSL
     */
    default SelectOptionStep<? extends Record> paging(@NonNull SelectConditionStep<? extends Record> sql,
                                                      Pagination pagination) {
        Pagination paging = Optional.ofNullable(pagination).orElseGet(() -> Pagination.builder().build());
        return sql.limit(paging.getPerPage()).offset((paging.getPage() - 1) * paging.getPerPage());
    }

}
