package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.DeviceMetadata;
import com.nubeiot.iotdata.edge.model.tables.daos.DeviceDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.records.DeviceRecord;

import lombok.NonNull;

public final class DeviceService
    extends AbstractDataPointService<UUID, Device, DeviceRecord, DeviceDao, DeviceMetadata> {

    public DeviceService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public DeviceMetadata metadata() {
        return DeviceMetadata.INSTANCE;
    }

}
