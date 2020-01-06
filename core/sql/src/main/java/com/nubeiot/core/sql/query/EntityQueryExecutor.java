package com.nubeiot.core.sql.query;

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
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * Represents for a {@code SQL  Executor} do {@code DML} or {@code DQL} on {@code entity}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @since 1.0.0
 */
//TODO lack unique keys validation
public interface EntityQueryExecutor<P extends VertxPojo> {

    /**
     * Sneaky throw database error in single type.
     *
     * @param throwable the throwable
     * @return error single
     * @since 1.0.0
     */
    @NonNull
    static Single sneakyThrowDBError(@NonNull Throwable throwable) {
        if (throwable instanceof TooManyRowsException) {
            return Single.error(new ImplementationError(ErrorCode.DATABASE_ERROR,
                                                        "Query is not correct, the result contains more than one " +
                                                        "record", throwable));
        }
        return Single.error(throwable);
    }

    /**
     * Sneaky throw database error in case of {@code unable delete} entity.
     *
     * @param clue the clue
     * @return error single
     * @since 1.0.0
     */
    @NonNull
    static Single unableDelete(String clue) {
        return Single.error(
            new NubeException("Cannot delete record", new HiddenException(ErrorCode.DATABASE_ERROR, clue)));
    }

    /**
     * Declares entity handler.
     *
     * @return the entity handler
     * @see EntityHandler
     * @since 1.0.0
     */
    @NonNull EntityHandler entityHandler();

    /**
     * Declares query builder.
     *
     * @return the query builder
     * @see QueryBuilder
     * @since 1.0.0
     */
    @NonNull QueryBuilder queryBuilder();

    /**
     * Create {@code DAO} based on given {@code dao class}.
     *
     * @param <K>      Type of {@code primary key}
     * @param <R>      Type of {@code UpdatableRecord}
     * @param <D>      Type of {@code VertxDAO}
     * @param daoClass the dao class
     * @return instance of {@code DAO}
     * @since 1.0.0
     */
    default <K, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> D dao(@NonNull Class<D> daoClass) {
        return entityHandler().dao(daoClass);
    }

    /**
     * Find many entity resources.
     *
     * @param requestData Request data
     * @return list pojo entities
     * @since 1.0.0
     */
    @NonNull Observable<P> findMany(@NonNull RequestData requestData);

    /**
     * Find one resource by {@code primary key} or by {@code composite unique key} after analyzing given request data
     *
     * @param requestData Request data
     * @return single pojo
     * @since 1.0.0
     */
    @NonNull Single<P> findOneByKey(@NonNull RequestData requestData);

    /**
     * Check whether resource is existed or not
     *
     * @param query Given query
     * @return empty if resource is not existed or {@code true}
     * @see QueryBuilder#exist(Table, Condition)
     * @since 1.0.0
     */
    @NonNull
    default Maybe<Boolean> fetchExists(@NonNull Function<DSLContext, Boolean> query) {
        return executeAny(query).filter(b -> b).switchIfEmpty(Maybe.empty());
    }

    /**
     * Get one resource by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return one single data source if found else throw {@code not found exception}
     * @see EntityMetadata#notFound(Object)
     * @since 1.0.0
     */
    @NonNull Single<P> lookupByPrimaryKey(@NonNull Object primaryKey);

    /**
     * Create new resource then return {@code primary key}
     *
     * @param requestData request data
     * @param validator   creation validator
     * @return DML pojo
     * @see DMLPojo
     * @since 1.0.0
     */
    @NonNull Single<DMLPojo> insertReturningPrimary(@NonNull RequestData requestData,
                                                    @NonNull OperationValidator validator);

    /**
     * Do update data on both {@code UPDATE} or {@code PATCH} action
     *
     * @param requestData Request data
     * @param validator   modification validator
     * @return DML pojo
     * @see DMLPojo
     * @since 1.0.0
     */
    @NonNull Single<DMLPojo> modifyReturningPrimary(@NonNull RequestData requestData,
                                                    @NonNull OperationValidator validator);

    /**
     * Do delete data by primary
     *
     * @param requestData Request data
     * @param validator   deletion validator
     * @return deleted resource
     * @since 1.0.0
     */
    @NonNull Single<P> deleteOneByKey(@NonNull RequestData requestData, @NonNull OperationValidator validator);

    /**
     * Check resource is able to delete by scanning reference resource to this resource
     *
     * @param pojo        Resource
     * @param metadata    Entity metadata
     * @param keyProvider function to search key from resource
     * @return single pojo or single existed error
     * @see EntityMetadata#unableDeleteDueUsing(String)
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    @NonNull
    default Single<P> isAbleToDelete(@NonNull P pojo, @NonNull EntityMetadata metadata,
                                     @NonNull Function<VertxPojo, String> keyProvider) {
        if (!(entityHandler() instanceof EntityConstraintHolder)) {
            return Single.just(pojo);
        }
        final Object pk = pojo.toJson().getValue(metadata.jsonKeyName());
        final EntityConstraintHolder holder = (EntityConstraintHolder) entityHandler();
        return Observable.fromIterable(holder.referenceTableKeysTo(metadata.table()))
                         .flatMapMaybe(e -> fetchExists(queryBuilder().exist(e.getKey(), e.getValue().eq(pk))))
                         .flatMap(b -> Observable.error(metadata.unableDeleteDueUsing(keyProvider.apply(pojo))))
                         .map(b -> pojo)
                         .defaultIfEmpty(pojo)
                         .singleOrError();
    }

    /**
     * Execute any function
     *
     * @param <X>      Type of {@code result}
     * @param function query function
     * @return result single
     * @apiNote Only using it in very complex case or special case
     * @see QueryBuilder#view(JsonObject, Sort, Pagination)
     * @see QueryBuilder#viewOne(JsonObject, Sort)
     * @see QueryBuilder#exist(Table, Condition)
     * @since 1.0.0
     */
    @NonNull <X> Single<X> executeAny(@NonNull Function<DSLContext, X> function);

}
