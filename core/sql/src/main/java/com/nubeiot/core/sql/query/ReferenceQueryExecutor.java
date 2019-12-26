package com.nubeiot.core.sql.query;

import java.util.Objects;
import java.util.Optional;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;

import lombok.NonNull;

/**
 * Represents for a {@code sql executor} do {@code DML} or {@code DQL} on {@code has reference entity}
 *
 * @param <P> Vertx pojo
 * @since 1.0.0
 */
public interface ReferenceQueryExecutor<P extends VertxPojo> extends SimpleQueryExecutor<P> {

    /**
     * Create reference query executor.
     *
     * @param <K>      Type of {@code primary key}
     * @param <P>      Type of {@code VertxPojo}
     * @param <R>      Type of {@code UpdatableRecord}
     * @param <D>      Type of {@code VertxDAO}
     * @param handler  the entity handler
     * @param metadata the metadata
     * @param marker   the reference entity marker
     * @return the reference query executor
     * @see EntityHandler
     * @since 1.0.0
     */
    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> ReferenceQueryExecutor create(
        @NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata,
        @NonNull ReferencingEntityMarker marker) {
        return new ReferenceDaoQueryExecutor<>(handler, metadata, marker);
    }

    /**
     * Defines {@code entity marker}.
     *
     * @return the has reference marker
     * @see ReferencingEntityMarker
     * @since 1.0.0
     */
    @NonNull ReferencingEntityMarker marker();

    /**
     * Verify {@code entity} whether exists or not.
     *
     * @param reqData the request data
     * @return error single if not found any {@code reference entity}, otherwise {@code true} single
     * @since 1.0.0
     */
    default Single<Boolean> checkReferenceExistence(@NonNull RequestData reqData) {
        final EntityReferences references = marker().referencedEntities();
        return Observable.fromIterable(references.getFields().entrySet()).flatMapSingle(entry -> {
            final EntityMetadata meta = entry.getKey();
            final Object key = findReferenceKey(reqData, meta, entry.getValue());
            return Objects.isNull(key)
                   ? Single.just(true)
                   : fetchExists(queryBuilder().exist(meta, key)).switchIfEmpty(Single.error(meta.notFound(key)));
        }).all(aBoolean -> aBoolean);
    }

    @SuppressWarnings("unchecked")
    default Object findReferenceKey(@NonNull RequestData reqData, @NonNull EntityMetadata metadata,
                                    @NonNull String refField) {
        return metadata.getKey(reqData)
                       .orElse(Optional.ofNullable(reqData.body().getValue(refField))
                                       .map(k -> metadata.parseKey(k.toString()))
                                       .orElse(null));
    }

}
