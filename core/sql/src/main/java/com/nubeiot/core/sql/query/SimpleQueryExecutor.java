package com.nubeiot.core.sql.query;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface SimpleQueryExecutor<P extends VertxPojo> extends EntityQueryExecutor<P> {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> SimpleQueryExecutor<P> create(
        @NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new SimpleDaoQueryExecutor<>(handler, metadata);
    }

}
