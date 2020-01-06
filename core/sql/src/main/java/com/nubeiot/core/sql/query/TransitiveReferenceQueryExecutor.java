package com.nubeiot.core.sql.query;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.marker.TransitiveReferenceMarker;

import lombok.NonNull;

/**
 * Represents for a {@code sql executor} do {@code DML} or {@code DQL} on {@code transitive reference entity}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @since 1.0.0
 */
public interface TransitiveReferenceQueryExecutor<P extends VertxPojo> extends ReferencingQueryExecutor<P> {

    /**
     * Create transitive reference query executor.
     *
     * @param <K>      Type of {@code primary key}
     * @param <P>      Type of {@code VertxPojo}
     * @param <R>      Type of {@code UpdatableRecord}
     * @param <D>      Type of {@code VertxDAO}
     * @param handler  the entity handler
     * @param metadata the metadata
     * @param marker   the transitive marker
     * @return the transitive reference query executor
     * @since 1.0.0
     */
    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> TransitiveReferenceQueryExecutor<P> create(
        @NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata,
        @NonNull TransitiveReferenceMarker marker) {
        return new TransitiveReferenceDaoQueryExecutor<>(handler, metadata, marker);
    }

    /**
     * @return transitive reference marker
     * @see TransitiveReferenceMarker
     * @since 1.0.0
     */
    @Override
    @NonNull TransitiveReferenceMarker marker();

}
