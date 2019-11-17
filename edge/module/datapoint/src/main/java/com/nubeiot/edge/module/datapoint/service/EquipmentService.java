package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EquipmentMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Equipment;

import lombok.NonNull;

public final class EquipmentService extends AbstractEntityService<Equipment, EquipmentMetadata>
    implements DataPointService<Equipment, EquipmentMetadata> {

    public EquipmentService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EquipmentMetadata context() {
        return EquipmentMetadata.INSTANCE;
    }

}
