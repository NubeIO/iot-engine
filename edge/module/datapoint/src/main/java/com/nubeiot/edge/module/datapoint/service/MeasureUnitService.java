package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.MeasureUnitMetadata;

import lombok.NonNull;

public final class MeasureUnitService extends AbstractDataPointService<MeasureUnitMetadata, MeasureUnitService> {

    public MeasureUnitService(@NonNull AbstractEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public MeasureUnitMetadata metadata() {
        return MeasureUnitMetadata.INSTANCE;
    }

}
