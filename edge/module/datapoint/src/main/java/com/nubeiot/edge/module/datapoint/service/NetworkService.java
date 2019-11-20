package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.EdgeService.EdgeExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class NetworkService extends AbstractOneToManyEntityService<Network, NetworkMetadata>
    implements DataPointService<Network, NetworkMetadata>, EdgeExtension {

    public NetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public NetworkMetadata context() {
        return NetworkMetadata.INSTANCE;
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        EventMethodDefinition d = EventMethodDefinition.createDefault("/edge/:edge_id/network", "/:network_id");
        return Stream.concat(DataPointService.super.definitions().stream(), Stream.of(d)).collect(Collectors.toSet());
    }

}
