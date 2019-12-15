package com.nubeiot.edge.module.datapoint.service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointThingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ThingMetadata;

import lombok.NonNull;

public final class ThingByPointService extends PointThingCompositeService {

    ThingByPointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public PointThingMetadata context() {
        return PointThingMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return PointMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return ThingMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.DQL_MAP.get().keySet();
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        final @NonNull Collection<EventAction> events = getAvailableEvents();
        return Stream.of(EntityHttpService.createDefinitions(events, resource(), true, EdgeMetadata.INSTANCE,
                                                             NetworkMetadata.INSTANCE, reference()),
                         EntityHttpService.createDefinitions(events, resource(), true, NetworkMetadata.INSTANCE,
                                                             reference()),
                         EntityHttpService.createDefinitions(events, resource(), reference()))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

}
