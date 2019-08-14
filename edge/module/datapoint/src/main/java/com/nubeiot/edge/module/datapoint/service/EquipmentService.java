package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.EquipmentMetadata;
import com.nubeiot.iotdata.edge.model.tables.daos.EquipmentDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Equipment;
import com.nubeiot.iotdata.edge.model.tables.records.EquipmentRecord;

import lombok.NonNull;

public final class EquipmentService
    extends AbstractDataPointService<UUID, Equipment, EquipmentRecord, EquipmentDao, EquipmentMetadata> {

    public EquipmentService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EquipmentMetadata metadata() {
        return EquipmentMetadata.INSTANCE;
    }

}
