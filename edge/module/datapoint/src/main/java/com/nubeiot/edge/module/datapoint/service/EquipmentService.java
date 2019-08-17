package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.EquipmentMetadata;

import lombok.NonNull;

public final class EquipmentService extends AbstractDataPointService<EquipmentMetadata, EquipmentService> {

    public EquipmentService(@NonNull AbstractEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EquipmentMetadata metadata() {
        return EquipmentMetadata.INSTANCE;
    }

}
