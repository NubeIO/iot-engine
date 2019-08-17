package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.ThingMetadata;

import lombok.NonNull;

public final class ThingService extends AbstractDataPointService<ThingMetadata, ThingService> {

    public ThingService(@NonNull AbstractEntityHandler entityHandler) {
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
