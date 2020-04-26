package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;

import lombok.NonNull;

public final class FolderByPointService extends AbstractFolderExtensionService {

    public FolderByPointService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return PointCompositeMetadata.INSTANCE;
    }

}
