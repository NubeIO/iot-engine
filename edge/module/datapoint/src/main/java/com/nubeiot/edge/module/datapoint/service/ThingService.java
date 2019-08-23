package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.datapoint.service.Metadata.ThingMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;

import lombok.NonNull;

public final class ThingService extends AbstractEntityService<Thing, ThingMetadata>
    implements DataPointService<Thing, ThingMetadata> {

    public ThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public ThingMetadata context() {
        return ThingMetadata.INSTANCE;
    }

}
