package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TransducerMetadata;

import lombok.NonNull;

public final class PointByTransducerService extends PointTransducerService {

    PointByTransducerService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return TransducerMetadata.INSTANCE;
    }

    @Override
    public @NonNull List<EntityMetadata> references() {
        return Arrays.asList(TransducerMetadata.INSTANCE, DeviceMetadata.INSTANCE, NetworkMetadata.INSTANCE);
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return PointMetadata.INSTANCE;
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        final @NonNull Collection<EventAction> events = getAvailableEvents();
        return Stream.of(super.definitions(),
                         EntityHttpService.createDefinitions(events, resource(), true, DeviceMetadata.INSTANCE,
                                                             reference()),
                         EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, events, resource(), true,
                                                             EdgeMetadata.INSTANCE, NetworkMetadata.INSTANCE,
                                                             DeviceMetadata.INSTANCE, reference()),
                         EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, events, resource(), true,
                                                             NetworkMetadata.INSTANCE, DeviceMetadata.INSTANCE,
                                                             reference()))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

}
