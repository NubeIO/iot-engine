package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.DeviceMetadata;

import lombok.NonNull;

public final class DeviceService extends AbstractDataPointService<DeviceMetadata, DeviceService> {

    public DeviceService(@NonNull AbstractEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public DeviceMetadata metadata() {
        return DeviceMetadata.INSTANCE;
    }

}
