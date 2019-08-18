package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.ThingMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;

import lombok.NonNull;

public final class ThingService extends AbstractDataPointService<Thing, ThingMetadata, ThingService> {

    public ThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public ThingMetadata metadata() {
        return ThingMetadata.INSTANCE;
    }

    @Override
    public ThingService validation() {
        return this;
    }

}
