package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.iotdata.dto.GroupLevel;

import lombok.NonNull;

public final class FolderByDeviceService extends FolderGroupService {

    public FolderByDeviceService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull GroupLevel groupLevel() {
        return GroupLevel.NETWORK;
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return FolderMetadata.INSTANCE;
    }

}
