package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.DeviceMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DeviceService extends AbstractDataPointService<Device, DeviceMetadata, DeviceService> {

    public DeviceService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public DeviceMetadata metadata() {
        return DeviceMetadata.INSTANCE;
    }

}
