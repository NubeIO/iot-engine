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
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.HasReferenceResource.EntityReferences;

import lombok.NonNull;

public interface ReferenceQueryExecutor<P extends VertxPojo> extends SimpleQueryExecutor<P> {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> ReferenceQueryExecutor create(
        @NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new ReferenceDaoQueryExecutor<>(handler, metadata);
    }

    @SuppressWarnings("unchecked")
    default Single<Boolean> mustExists(@NonNull RequestData reqData, @NonNull HasReferenceResource ref) {
        final EntityReferences references = ref.entityReferences();
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
