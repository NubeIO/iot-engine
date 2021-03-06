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
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TransducerMetadata;

import lombok.NonNull;

public final class TransducerByPointService extends PointTransducerService {

    TransducerByPointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return TransducerMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.DQL_MAP.get().keySet();
    }

    @Override
    public boolean supportForceDeletion() {
        return false;
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        final @NonNull Collection<EventAction> events = getAvailableEvents();
        return Stream.of(super.definitions(),
                         EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, events, resource(), true,
                                                             EdgeMetadata.INSTANCE, NetworkMetadata.INSTANCE,
                                                             reference()),
                         EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, events, resource(), true,
                                                             NetworkMetadata.INSTANCE, reference()))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

}
