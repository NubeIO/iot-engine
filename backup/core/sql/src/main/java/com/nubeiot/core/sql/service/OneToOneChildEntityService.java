package com.nubeiot.core.sql.service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.marker.OneToOneEntityMarker;

import lombok.NonNull;

/**
 * Represents for an entity service that has {@code one-to-one} relationship to other entities and in business context,
 * it is as {@code child} entity.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see ReferencingEntityService
 * @see OneToOneEntityMarker
 * @since 1.0.0
 */
public interface OneToOneChildEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends ReferencingEntityService<P, M>, OneToOneEntityMarker {

    static Set<EventAction> availableEvents(@NonNull Collection<EventAction> availableEvents) {
        return availableEvents.stream().filter(action -> action != EventAction.GET_LIST).collect(Collectors.toSet());
    }

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return availableEvents(ReferencingEntityService.super.getAvailableEvents());
    }

}
