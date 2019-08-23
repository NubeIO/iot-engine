package com.nubeiot.core.sql.query;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface ReferenceQueryExecutor<P extends VertxPojo> extends SimpleQueryExecutor<P> {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> ReferenceQueryExecutor create(
        EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new DaoReferenceQueryExecutor<>(handler, metadata);
    }

    class DaoReferenceQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>>
        extends DaoQueryExecutor<K, P, R, D> implements ReferenceQueryExecutor<P> {

        DaoReferenceQueryExecutor(EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
            super(handler, metadata);
        }

        @Override
        public Single<P> findOneByKey(RequestData requestData) {
            K pk = metadata.parseKey(requestData);
            return handler.dao(metadata.daoClass())
                          .queryExecutor().findOne(viewOneQuery(requestData.getFilter()))
                          .flatMap(o -> o.map(Single::just).orElse(Single.error(metadata.notFound(pk))))
                          .onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError);
        }

    }

}
