package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents a service delegate that wraps an actual {@code entity service} in case of it cannot {@code extends}
 * directly default {@code entity service}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @param <S> Type of {@code EntityService}
 * @see EntityService
 * @see ReferencingEntityService
 * @see GroupEntityService
 * @see ManyToManyEntityService
 * @since 1.0.0
 */
@RequiredArgsConstructor
public abstract class AbstractEntityServiceDelegate<P extends VertxPojo, M extends EntityMetadata,
                                                       S extends EntityService<P, M>>
    implements EntityServiceDelegate<P, M, S> {

    @NonNull
    private final S service;

    public S unwrap() {
        return this.service;
    }

}
