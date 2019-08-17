package com.nubeiot.core.sql.query;

import org.jooq.UpdatableRecord;
import org.jooq.exception.TooManyRowsException;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface ReferenceQueryExecutor extends SimpleQueryExecutor {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> ReferenceQueryExecutor create(
        EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new DaoReferenceQueryExecutor<>(handler, metadata);
    }

    static Maybe wrapDatabaseError(Throwable t) {
        return Maybe.error(t instanceof TooManyRowsException ? new StateException(
            "Query is not correct, the result contains more than one record", t) : t);
    }

    class DaoReferenceQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>>
        extends DaoQueryExecutor<K, P, R, D> implements ReferenceQueryExecutor {

        private DaoReferenceQueryExecutor(EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
            super(handler, metadata);
        }

        @Override
        public Maybe<? extends VertxPojo> findOne(RequestData requestData) {
            K pk = metadata.parsePrimaryKey(requestData);
            return handler.dao(metadata.daoClass())
                          .queryExecutor()
                          .findOne(ctx -> query(ctx, requestData))
                          .flatMapMaybe(o -> o.map(Maybe::just).orElse(Maybe.error(metadata.notFound(pk))))
                          .onErrorResumeNext(ReferenceQueryExecutor::wrapDatabaseError)
                          .map(p -> p);
        }

    }

}
