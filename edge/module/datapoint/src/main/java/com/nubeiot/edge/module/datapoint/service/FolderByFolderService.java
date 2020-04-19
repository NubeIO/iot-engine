package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.iotdata.dto.GroupLevel;

import lombok.NonNull;

//TODO recursive folder view later
final class FolderByFolderService extends FolderGroupService {

    public FolderByFolderService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull GroupLevel groupLevel() {
        return GroupLevel.FOLDER;
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return FolderGroupMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return FolderGroupMetadata.INSTANCE;
    }

}
