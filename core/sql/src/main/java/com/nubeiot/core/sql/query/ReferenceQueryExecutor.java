package com.nubeiot.core.sql.query;

import java.util.Objects;
import java.util.Optional;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;

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
    default Maybe<Boolean> mustExists(@NonNull RequestData reqData, @NonNull HasReferenceResource ref) {
        final EntityReferences references = ref.entityReferences();
        return Observable.fromIterable(references.getFields().entrySet()).flatMapMaybe(entry -> {
            final EntityMetadata meta = entry.getKey();
            Object key = meta.getKey(reqData)
                             .orElse(Optional.ofNullable(reqData.body().getValue(entry.getValue().toLowerCase()))
                                             .map(k -> meta.parseKey(k.toString()))
                                             .orElse(null));
            if (Objects.isNull(key)) {
                return Maybe.just(true);
            }
            return fetchExists(existQuery(meta, key)).switchIfEmpty(Maybe.error(meta.notFound(key)));
        }).reduce((b1, b2) -> b1 && b2);
    }

}
