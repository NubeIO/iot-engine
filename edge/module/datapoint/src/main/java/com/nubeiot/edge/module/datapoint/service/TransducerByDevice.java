package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceThingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TransducerMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.ThingComposite;
import com.nubeiot.iotdata.edge.model.tables.Thing;

import lombok.NonNull;

public final class TransducerByDevice extends AbstractManyToManyEntityService<ThingComposite, DeviceThingMetadata>
    implements DataPointService<ThingComposite, DeviceThingMetadata> {

    public TransducerByDevice(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public DeviceThingMetadata context() {
        return DeviceThingMetadata.INSTANCE;
    }

    @Override
    public EntityReferences entityReferences() {
        final @NonNull Thing table = context().table();
        return new EntityReferences().add(reference(), table.getJsonField(table.DEVICE))
                                     .add(resource(), table.getJsonField(table.TRANSDUCER));
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return TransducerMetadata.INSTANCE;
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), resource(), reference());
    }

}
