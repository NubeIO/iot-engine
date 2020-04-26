package com.nubeiot.core.sql.query;

import java.util.function.Function;

import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.exception.TooManyRowsException;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter;
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
import com.nubeiot.core.utils.JsonUtils;

import lombok.NonNull;

/**
 * Represents for a {@code SQL  Executor} do {@code DML} or {@code DQL} on {@code entity}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @since 1.0.0
 */
//TODO lack unique keys validation. https://github.com/NubeIO/iot-engine/issues/321
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

    @NonNull Configuration runtimeConfiguration();

    @NonNull EntityQueryExecutor runtimeConfiguration(Configuration configuration);

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
     * @param metadata the entity metadata
     * @return instance of {@code DAO}
     * @since 1.0.0
     */
    default <K, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> D dao(EntityMetadata<K, P, R, D> metadata) {
        return entityHandler().dao(runtimeConfiguration(), metadata.daoClass());
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
     * Do delete data by {@code primary key}
     *
     * @param requestData Request data
     * @param validator   deletion validator
     * @return deleted resource
     * @since 1.0.0
     */
    @NonNull Single<P> deleteOneByKey(@NonNull RequestData requestData, @NonNull OperationValidator validator);

    /**
     * Execute any function
     *
     * @param <X>      Type of {@code result}
     * @param function query function
     * @return result single
     * @apiNote Only using it in very complex case or special case
     * @see QueryBuilder#view(RequestFilter, Sort, Pagination)
     * @see QueryBuilder#viewOne(RequestFilter, Sort)
     * @see QueryBuilder#exist(Table, Condition)
     * @since 1.0.0
     */
    @NonNull <X> Single<X> executeAny(@NonNull Function<DSLContext, X> function);

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

    @NonNull
    default Maybe<Boolean> fetchExists(@NonNull Table table, @NonNull Condition condition) {
        return fetchExists(queryBuilder().exist(table, condition));
    }

    /**
     * Check resource is able to delete by scanning reference resource to this resource
     *
     * @param pojo     Resource
     * @param metadata Entity metadata
     * @return single pojo or single existed error
     * @see EntityMetadata#unableDeleteDueUsing(String)
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    @NonNull
    default Single<Boolean> isAbleToDelete(@NonNull P pojo, @NonNull EntityMetadata metadata) {
        final EntityConstraintHolder holder = entityHandler().holder();
        if (EntityConstraintHolder.BLANK == holder) {
            return Single.just(true);
        }
        final Object pk = metadata.parseKey(pojo);
        return Observable.fromIterable(holder.referenceTo(metadata.table()))
                         .flatMapMaybe(ref -> fetchExists(queryBuilder().exist(ref.getTable(), ref.getField().eq(pk))))
                         .flatMap(b -> Observable.error(
                             metadata.unableDeleteDueUsing(requestKeyAsMessage(metadata, pojo, pk))))
                         .map(Boolean.class::cast)
                         .defaultIfEmpty(true)
                         .singleOrError();
    }

    default @NonNull String requestKeyAsMessage(@NonNull EntityMetadata metadata, @NonNull VertxPojo pojo,
                                                @NonNull Object primaryKey) {
        return JsonUtils.kvMsg(metadata.requestKeyName(), primaryKey);
    }

}
