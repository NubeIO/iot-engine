package com.nubeiot.core.sql.query;

import java.util.Collection;
import java.util.Objects;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface ReferenceQueryExecutor<P extends VertxPojo> extends SimpleQueryExecutor<P> {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> ReferenceQueryExecutor create(
        @NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new ReferenceDaoQueryExecutor<>(handler, metadata);
    }

    default Maybe<Boolean> mustExists(@NonNull RequestData reqData, @NonNull Collection<EntityMetadata> refMetadata) {
        return Observable.fromIterable(refMetadata).filter(Objects::nonNull).flatMapMaybe(meta -> {
            Object key = meta.parseKey(reqData);
            return fetchExists(existQuery(meta, key)).switchIfEmpty(Maybe.error(meta.notFound(key)));
        }).reduce((b1, b2) -> b1 && b2);
    }

}
