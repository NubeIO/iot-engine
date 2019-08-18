package com.nubeiot.core.sql.query;

import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
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

public interface SimpleQueryExecutor<P extends VertxPojo> extends EntityQueryExecutor<P> {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> SimpleQueryExecutor create(
        EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new DaoQueryExecutor<>(handler, metadata);
    }

    @SuppressWarnings("unchecked")
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    class DaoQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>>
        implements SimpleQueryExecutor<P> {

        protected final EntityHandler handler;
        protected final EntityMetadata<K, P, R, D> metadata;

        @Override
        public Observable<P> findMany(RequestData requestData) {
            return handler.dao(metadata.daoClass())
                          .queryExecutor()
                          .findMany(ctx -> query(ctx, requestData))
                          .flattenAsObservable(records -> records);
        }

        @Override
        public Single<P> findOneByKey(RequestData requestData) {
            K pk = metadata.parsePrimaryKey(requestData);
            return handler.dao(metadata.daoClass())
                          .findOneById(pk)
                          .flatMap(o -> o.map(Single::just).orElse(Single.error(metadata.notFound(pk))));
        }

        @Override
        public Maybe<P> lookupByPrimaryKey(@NonNull Object primaryKey) {
            return handler.dao(metadata.daoClass())
                          .findOneById((K) primaryKey)
                          .flatMapMaybe(o -> o.map(Maybe::just).orElse(Maybe.error(metadata.notFound(primaryKey))));
        }

        @Override
        public Single<K> insertReturningPrimary(VertxPojo pojo, RequestData requestData) {
            return handler.dao(metadata.daoClass()).insertReturningPrimary((P) pojo);
        }

        @Override
        public Single<K> modifyReturningPrimary(RequestData requestData, EventAction action,
                                                Function3<VertxPojo, VertxPojo, JsonObject, VertxPojo> validation) {
            final K pk = metadata.parsePrimaryKey(requestData);
            final D dao = handler.dao(metadata.daoClass());
            return findOneByKey(requestData).map(
                db -> validation.apply(db, metadata.parse(requestData.body().put(metadata.jsonKeyName(), pk)),
                                       requestData.headers()))
                                            .map(p -> (P) p)
                                            .flatMap(dao::update)
                                            .filter(i -> i > 0)
                                            .switchIfEmpty(Single.error(metadata.notFound(pk)))
                                            .map(i -> pk);
        }

        @Override
        public Maybe<P> deleteByPrimary(P pojo, @NonNull Object pk) {
            return handler.dao(metadata.daoClass())
                          .deleteById((K) pk)
                          .flatMapMaybe(r -> r > 0 ? Maybe.just(pojo) : Maybe.error(metadata.notFound(pk)));
        }

        /**
         * Do query data
         *
         * @param ctx         DSL Context
         * @param requestData Request data
         * @return result query
         * @see #paging(SelectConditionStep, Pagination)
         */
        protected ResultQuery<R> query(@NonNull DSLContext ctx, @NonNull RequestData requestData) {
            return (ResultQuery<R>) paging(filter(ctx, metadata.table(), requestData.getFilter()),
                                           requestData.getPagination());
        }

    }

}
