package com.nubeiot.core.sql.query;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.utils.Strings;

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
        public EntityHandler entityHandler() {
            return handler;
        }

        @Override
        public Observable<P> findMany(RequestData reqData) {
            final Pagination paging = Optional.ofNullable(reqData.getPagination()).orElse(Pagination.builder().build());
            return handler.dao(metadata.daoClass())
                          .queryExecutor().findMany(viewQuery(reqData.getFilter(), paging))
                          .flattenAsObservable(records -> records);
        }

        @Override
        public Single<P> findOneByKey(RequestData requestData) {
            K pk = metadata.parseKey(requestData);
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
        public Single<K> insertReturningPrimary(VertxPojo pojo, RequestData reqData) {
            //TODO validate unique keys
            return handler.dao(metadata.daoClass())
                          .insertReturningPrimary((P) AuditDecorator.addCreationAudit(reqData, metadata, pojo));
        }

        @Override
        public Single<K> modifyReturningPrimary(RequestData reqData, EventAction action,
                                                BiFunction<VertxPojo, RequestData, VertxPojo> validator) {
            final K pk = metadata.parseKey(reqData);
            final D dao = handler.dao(metadata.daoClass());
            //TODO validate unique keys
            return findOneByKey(reqData).map(
                db -> (P) AuditDecorator.addModifiedAudit(reqData, metadata, db, validator.apply(db, reqData)))
                                        .flatMap(dao::update)
                                        .filter(i -> i > 0)
                                        .switchIfEmpty(Single.error(metadata.notFound(pk)))
                                        .map(i -> pk);
        }

        @Override
        public Single<P> deleteOneByKey(RequestData reqData) {
            final K pk = metadata.parseKey(reqData);
            return findOneByKey(reqData).flatMap(dbPojo -> isAbleToDelete(dbPojo, metadata, this::pojoKeyMsg))
                                        .flatMap(pojo -> handler.dao(metadata.daoClass())
                                                                .deleteById(pk)
                                                                .filter(r -> r > 0)
                                                                .map(r -> pojo)
                                                                .switchIfEmpty(EntityQueryExecutor.unableDelete(
                                                                    Strings.kvMsg(metadata.requestKeyName(), pk))));
        }

        @Override
        public Function<DSLContext, ResultQuery<R>> viewQuery(JsonObject filter, Pagination pagination) {
            return ctx -> (ResultQuery<R>) paging(
                ctx.select().from(metadata.table()).where(condition(metadata.table(), filter)), pagination);
        }

        @Override
        public Function<DSLContext, ResultQuery<R>> viewOneQuery(JsonObject filter) {
            return viewQuery(filter, Pagination.oneValue());
        }

        protected String pojoKeyMsg(VertxPojo pojo) {
            return Strings.kvMsg(metadata.requestKeyName(), pojo.toJson().getValue(metadata.jsonKeyName()));
        }

    }

}
