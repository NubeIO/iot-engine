package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;

import lombok.NonNull;

public final class FolderByDeviceService extends AbstractFolderExtensionService {

    public FolderByDeviceService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return DeviceMetadata.INSTANCE;
    }

}
