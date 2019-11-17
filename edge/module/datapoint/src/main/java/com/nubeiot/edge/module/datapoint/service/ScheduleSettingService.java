package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.SchedulerSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.ScheduleSetting;

import lombok.NonNull;

public final class ScheduleSettingService
    extends AbstractOneToManyEntityService<ScheduleSetting, SchedulerSettingMetadata>
    implements PointExtension, DataPointService<ScheduleSetting, SchedulerSettingMetadata> {

    public ScheduleSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public String servicePath() {
        return "/schedules";
    }

    @Override
    public SchedulerSettingMetadata context() {
        return SchedulerSettingMetadata.INSTANCE;
    }

}
