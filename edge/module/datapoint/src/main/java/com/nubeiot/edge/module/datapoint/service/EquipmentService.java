package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.EquipmentMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Equipment;

import lombok.NonNull;

public final class EquipmentService extends AbstractDataPointService<Equipment, EquipmentMetadata, EquipmentService> {

    public EquipmentService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EquipmentMetadata metadata() {
        return EquipmentMetadata.INSTANCE;
    }

}
