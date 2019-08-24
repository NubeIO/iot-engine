package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;

import lombok.NonNull;

public final class RealtimeSettingService
    extends AbstractOneToManyEntityService<RealtimeSetting, RealtimeSettingMetadata>
    implements PointExtension, DataPointService<RealtimeSetting, RealtimeSettingMetadata> {

    public RealtimeSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public RealtimeSettingMetadata context() {
        return RealtimeSettingMetadata.INSTANCE;
    }

}
