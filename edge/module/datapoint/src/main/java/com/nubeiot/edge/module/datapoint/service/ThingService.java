package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ThingMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;

import lombok.NonNull;

public final class ThingService extends AbstractOneToManyEntityService<Thing, ThingMetadata>
    implements DataPointService<Thing, ThingMetadata> {

    public ThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public ThingMetadata context() {
        return ThingMetadata.INSTANCE;
    }

    @Override
    public EntityReferences entityReferences() {
        final @NonNull com.nubeiot.iotdata.edge.model.tables.Thing table = context().table();
        return new EntityReferences().add(DeviceMetadata.INSTANCE, table.getJsonField(table.DEVICE_ID));
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.concat(DataPointService.super.definitions().stream(),
                             EntityHttpService.createDefinitions(getAvailableEvents(), context(),
                                                                 DeviceMetadata.INSTANCE).stream())
                     .collect(Collectors.toSet());
    }

}
