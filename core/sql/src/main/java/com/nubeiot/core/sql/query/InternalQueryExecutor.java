package com.nubeiot.core.sql.query;

import java.util.Objects;
import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

interface InternalQueryExecutor<P extends VertxPojo> extends EntityQueryExecutor<P> {

    EntityMetadata getMetadata();

    @Override
    @SuppressWarnings("unchecked")
    default Single<P> lookupByPrimaryKey(@NonNull Object primaryKey) {
        return lookupByPrimaryKey(getMetadata(), primaryKey).flatMap(
            o -> o.map(Single::just).orElse(Single.error(getMetadata().notFound(primaryKey)))).map(p -> (P) p);
    }

    @SuppressWarnings("unchecked")
    default Single<Optional<? extends VertxPojo>> lookupByPrimaryKey(@NonNull EntityMetadata metadata, Object key) {
        return Objects.isNull(key)
               ? Single.just(Optional.empty())
               : (Single<Optional<? extends VertxPojo>>) metadata.dao(entityHandler()).findOneById(key);
    }

}
