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
import com.nubeiot.core.sql.service.HasReferenceMarker;
import com.nubeiot.core.sql.service.HasReferenceMarker.EntityReferences;

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
        @NonNull HasReferenceMarker marker) {
        return new ReferenceDaoQueryExecutor<>(handler, metadata, marker);
    }

    /**
     * Defines {@code entity marker}.
     *
     * @return the has reference marker
     * @see HasReferenceMarker
     * @since 1.0.0
     */
    @NonNull HasReferenceMarker marker();

    /**
     * Verify {@code entity} whether exists or not.
     *
     * @param reqData the request data
     * @return boolean single
     * @see HasReferenceMarker
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default Single<Boolean> mustExists(@NonNull RequestData reqData) {
        final EntityReferences references = marker().entityReferences();
        return Observable.fromIterable(references.getFields().entrySet()).flatMapSingle(entry -> {
            final EntityMetadata meta = entry.getKey();
            Object key = meta.getKey(reqData)
                             .orElse(Optional.ofNullable(reqData.body().getValue(entry.getValue().toLowerCase()))
                                             .map(k -> meta.parseKey(k.toString()))
                                             .orElse(null));
            if (Objects.isNull(key)) {
                return Single.just(true);
            }
            return fetchExists(queryBuilder().exist(meta, key)).switchIfEmpty(Single.error(meta.notFound(key)));
        }).all(aBoolean -> aBoolean);
    }

}
