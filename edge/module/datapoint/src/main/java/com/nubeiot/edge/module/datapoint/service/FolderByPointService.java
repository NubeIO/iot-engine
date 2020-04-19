package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.iotdata.dto.GroupLevel;

import lombok.NonNull;

public final class FolderByPointService extends FolderGroupService {

    public FolderByPointService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull GroupLevel groupLevel() {
        return GroupLevel.DEVICE;
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return FolderMetadata.INSTANCE;
    }

}
