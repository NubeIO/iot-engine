package com.nubeiot.core.sql.query;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOptionStep;
import org.jooq.exception.TooManyRowsException;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function3;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.EntityMetadata;
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
     * @return primary key
     */
    Single<?> modifyReturningPrimary(@NonNull RequestData requestData, @NonNull EventAction action,
                                     @NonNull Function3<VertxPojo, VertxPojo, JsonObject, VertxPojo> validation);

    /**
     * Do delete data by primary
     *
     * @return deleted resource
     */
    Maybe<P> deleteByPrimary(@NonNull P pojo, @NonNull Object pk);

    /**
     * Do query filter
     * <p>
     * It is simple filter function by equal comparision. Any complex query should be override by each service.
     *
     * @param context Select condition step command
     * @param filter  Filter request
     * @return Database Select DSL
     * @see SelectConditionStep
     */
    //TODO Rich query depends on RQL in future https://github.com/NubeIO/iot-engine/issues/128
    @SuppressWarnings("unchecked")
    default SelectConditionStep<? extends Record> filter(DSLContext context, JsonTable<? extends Record> table,
                                                         JsonObject filter) {
        final SelectConditionStep<? extends Record> where = context.selectFrom(table).where(DSL.trueCondition());
        if (Objects.isNull(filter)) {
            return where;
        }
        final Map<String, String> jsonFields = table.jsonFields();
        filter.stream().map(entry -> {
            final Field field = table.field(jsonFields.getOrDefault(entry.getKey(), entry.getKey()));
            return Optional.ofNullable(field)
                           .map(f -> Optional.ofNullable(entry.getValue()).map(f::eq).orElseGet(f::isNull))
                           .orElse(null);
        }).filter(Objects::nonNull).forEach(where::and);
        return where;
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
