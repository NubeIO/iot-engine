package com.nubeiot.core.sql.query;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.exception.TooManyRowsException;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;

import lombok.NonNull;

//TODO lack unique keys validation
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

    QueryBuilder queryBuilder();

    default <K, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> D dao(Class<D> daoClass) {
        return entityHandler().dao(daoClass);
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
     * Check whether resource is existed or not
     *
     * @param query Given query
     * @return empty if resource is not existed or {@code true}
     * @see QueryBuilder#exist(Table, Condition)
     */
    default Maybe<Boolean> fetchExists(@NonNull Function<DSLContext, Boolean> query) {
        return executeAny(query).filter(b -> b).switchIfEmpty(Maybe.empty());
    }

    /**
     * Get one resource by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return one single data source if found else throw {@code not found exception}
     * @see EntityMetadata#notFound(Object)
     */
    Single<P> lookupByPrimaryKey(@NonNull Object primaryKey);

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
     * Check resource is able to delete by scanning reference resource to this resource
     *
     * @param pojo        Resource
     * @param metadata    Entity metadata
     * @param keyProvider key provider to search
     * @return single pojo or single existed error
     * @see EntityMetadata#unableDeleteDueUsing(String)
     */
    @SuppressWarnings("unchecked")
    default Single<P> isAbleToDelete(@NonNull P pojo, @NonNull EntityMetadata metadata,
                                     Function<VertxPojo, String> keyProvider) {
        if (!(entityHandler() instanceof EntityConstraintHolder)) {
            return Single.just(pojo);
        }
        final Object pk = pojo.toJson().getValue(metadata.jsonKeyName());
        final EntityConstraintHolder holder = (EntityConstraintHolder) entityHandler();
        return Observable.fromIterable(holder.referenceTableKeysTo(metadata.table()))
                         .flatMapMaybe(e -> fetchExists(queryBuilder().exist(e.getKey(), e.getValue().eq(pk))))
                         .flatMap(b -> Observable.error(metadata.unableDeleteDueUsing(keyProvider.apply(pojo))))
                         .map(b -> pojo)
                         .switchIfEmpty(Observable.just(pojo))
                         .singleOrError();
    }

    /**
     * Execute any function
     *
     * @param function query function
     * @param <X>      Result type
     * @return single of result
     * @apiNote Only using it in very complex case or special case
     * @see QueryBuilder#view(JsonObject, Sort, Pagination)
     * @see QueryBuilder#viewOne(JsonObject)
     * @see QueryBuilder#exist(Table, Condition)
     */
    <X> Single<X> executeAny(@NonNull Function<DSLContext, X> function);

}
