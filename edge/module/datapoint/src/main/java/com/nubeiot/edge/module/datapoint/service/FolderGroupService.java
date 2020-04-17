package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;

public abstract class FolderGroupService extends AbstractEntityService<FolderGroup, FolderGroupMetadata>
    implements DataPointService<FolderGroup, FolderGroupMetadata> {

    public FolderGroupService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public FolderGroupMetadata context() {
        return FolderGroupMetadata.INSTANCE;
    }

}
