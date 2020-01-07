package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.edge.module.datapoint.service.extension.EdgeExtension;
import com.nubeiot.edge.module.datapoint.service.extension.NetworkExtension;
import com.nubeiot.iotdata.edge.model.tables.EdgeDevice;

import lombok.NonNull;

public final class DeviceByNetworkService
    extends AbstractManyToManyEntityService<EdgeDeviceComposite, EdgeDeviceMetadata>
    implements DataPointService<EdgeDeviceComposite, EdgeDeviceMetadata> {

    public DeviceByNetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EdgeDeviceMetadata context() {
        return EdgeDeviceMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return NetworkMetadata.INSTANCE;
    }

    @Override
    public @NonNull List<EntityMetadata> references() {
        return Arrays.asList(reference(), EdgeMetadata.INSTANCE);
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public Set<String> ignoreFields() {
        return Stream.concat(super.ignoreFields().stream(), Stream.of(getEdgeField())).collect(Collectors.toSet());
    }

    @Override
    public @NonNull RequestDecorator requestDecorator() {
        return NetworkExtension.create(EdgeExtension.create(this));
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), resource(), EdgeMetadata.INSTANCE,
                                                   NetworkMetadata.INSTANCE);
    }

    private String getEdgeField() {
        final @NonNull EdgeDevice table = context().table();
        return table.getJsonField(table.EDGE_ID);
    }

}
