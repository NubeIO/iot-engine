package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Folder;

public final class FolderService extends AbstractEntityService<Folder, FolderMetadata>
    implements DataPointService<Folder, FolderMetadata> {

    public FolderService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public FolderMetadata context() {
        return FolderMetadata.INSTANCE;
    }

    @Override
    public boolean supportForceDeletion() {
        return true;
    }

}
