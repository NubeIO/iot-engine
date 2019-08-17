package com.nubeiot.core.sql.query;

import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function3;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface SimpleQueryExecutor extends EntityQueryExecutor {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> SimpleQueryExecutor create(
        EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new DaoQueryExecutor<>(handler, metadata);
    }

    @SuppressWarnings("unchecked")
    @RequiredArgsConstructor(access = AccessLevel.MODULE)
    class DaoQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>>
        implements SimpleQueryExecutor {

        protected final EntityHandler handler;
        protected final EntityMetadata<K, P, R, D> metadata;

        @Override
        public Observable<VertxPojo> findMany(RequestData requestData) {
            return handler.dao(metadata.daoClass())
                          .queryExecutor()
                          .findMany(ctx -> query(ctx, requestData))
                          .flattenAsObservable(records -> records);
        }

        @Override
        public Maybe<? extends VertxPojo> findOne(RequestData requestData) {
            K pk = metadata.parsePrimaryKey(requestData);
            return handler.dao(metadata.daoClass())
                          .findOneById(pk)
                          .flatMapMaybe(o -> o.map(Maybe::just).orElse(Maybe.error(metadata.notFound(pk))));
        }

        @Override
        public Maybe<? extends VertxPojo> lookupById(@NonNull Object primaryKey) {
            return handler.dao(metadata.daoClass())
                          .findOneById((K) primaryKey)
                          .flatMapMaybe(o -> o.map(Maybe::just).orElse(Maybe.error(metadata.notFound(primaryKey))));
        }

        @Override
        public Single<K> insertReturningPrimary(VertxPojo pojo) {
            return handler.dao(metadata.daoClass()).insertReturningPrimary((P) pojo);
        }

        @Override
        public Single<K> modifyReturningPrimary(RequestData requestData, EventAction action,
                                                Function3<VertxPojo, VertxPojo, JsonObject, VertxPojo> validation) {
            final K pk = metadata.parsePrimaryKey(requestData);
            final D dao = handler.dao(metadata.daoClass());
            return findOne(requestData).toSingle()
                                       .map(db -> validation.apply(db, metadata.parse(
                                           requestData.body().put(metadata.jsonKeyName(), pk)), requestData.headers()))
                                       .map(p -> (P) p)
                                       .flatMap(dao::update)
                                       .filter(i -> i > 0)
                                       .switchIfEmpty(Single.error(metadata.notFound(pk)))
                                       .map(i -> pk);
        }

        @Override
        public <R extends Record> Single<QueryResult> query(
            Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
            return handler.genericQuery().query(queryFunction);
        }

        @Override
        public Single<Integer> execute(Function<DSLContext, ? extends Query> queryFunction) {
            return handler.genericQuery().execute(queryFunction);
        }

        @Override
        public <X> Single<X> executeAny(Function<DSLContext, X> function) {
            return handler.genericQuery().executeAny(function);
        }

        /**
         * Do query data
         *
         * @param ctx         DSL Context
         * @param requestData Request data
         * @return result query
         * @see #paging(SelectConditionStep, Pagination)
         */
        @SuppressWarnings("unchecked")
        protected ResultQuery<R> query(@NonNull DSLContext ctx, @NonNull RequestData requestData) {
            return (ResultQuery<R>) paging(filter(ctx, metadata.table(), requestData.getFilter()),
                                           requestData.getPagination());
        }

    }

}
