package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.OneToManyReferenceEntityService;
import com.nubeiot.edge.module.datapoint.service.Metadata.SchedulerSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.daos.ScheduleSettingDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.ScheduleSetting;
import com.nubeiot.iotdata.edge.model.tables.records.ScheduleSettingRecord;

import lombok.NonNull;

public final class ScheduleSettingService extends
                                          AbstractDataPointService<UUID, ScheduleSetting, ScheduleSettingRecord,
                                                                      ScheduleSettingDao, SchedulerSettingMetadata>
    implements
    OneToManyReferenceEntityService<UUID, ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao,
                                       SchedulerSettingMetadata>,
    PointExtension {

    public ScheduleSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public String servicePath() {
        return "/schedules";
    }

    @Override
    public SchedulerSettingMetadata metadata() {
        return SchedulerSettingMetadata.INSTANCE;
    }

}
