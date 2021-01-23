package com.nubeiot.core.sql.query;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

/**
 * Represents for a {@code sql executor} do {@code DML} or {@code DQL} on {@code simple entity}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @since 1.0.0
 */
public interface SimpleQueryExecutor<P extends VertxPojo> extends EntityQueryExecutor<P> {

    /**
     * Create simple query executor.
     *
     * @param <K>      Type of {@code primary key}
     * @param <P>      Type of {@code VertxPojo}
     * @param <R>      Type of {@code UpdatableRecord}
     * @param <D>      Type of {@code VertxDAO}
     * @param handler  the entity handler
     * @param metadata the metadata
     * @return the simple query executor
     * @see EntityHandler
     * @since 1.0.0
     */
    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> SimpleQueryExecutor<P> create(
        @NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new SimpleDaoQueryExecutor<>(handler, metadata);
    }

}
