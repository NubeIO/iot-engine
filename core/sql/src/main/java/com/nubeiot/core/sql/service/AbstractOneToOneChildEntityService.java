package com.nubeiot.core.sql.service;

import java.util.Collection;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.HasReferenceRequestDecorator;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code database entity} has a {@code one-to-one}
 * relationship, and is {@code child} role.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @since 1.0.0
 */
public abstract class AbstractOneToOneChildEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends AbstractReferencingEntityService<P, M>
    implements HasReferenceRequestDecorator, OneToOneChildEntityService<P, M> {

    /**
     * Instantiates a new Abstract one to many entity service.
     *
     * @param entityHandler the entity handler
     * @since 1.0.0
     */
    public AbstractOneToOneChildEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return OneToOneChildEntityService.super.getAvailableEvents();
    }

}
