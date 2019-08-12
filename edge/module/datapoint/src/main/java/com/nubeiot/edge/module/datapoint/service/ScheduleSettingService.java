package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.HasReferenceEntityService;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.ScheduleSettingDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.ScheduleSetting;
import com.nubeiot.iotdata.edge.model.tables.records.ScheduleSettingRecord;

import lombok.NonNull;

public final class ScheduleSettingService
    extends AbstractDataPointService<UUID, ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao>
    implements UUIDKeyEntity<ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao>,
               HasReferenceEntityService<UUID, ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao>,
               PointExtension {

    public ScheduleSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "schedules";
    }

    @Override
    public @NonNull Class<ScheduleSetting> modelClass() {
        return ScheduleSetting.class;
    }

    @Override
    public @NonNull Class<ScheduleSettingDao> daoClass() {
        return ScheduleSettingDao.class;
    }

    @Override
    public @NonNull JsonTable<ScheduleSettingRecord> table() {
        return Tables.SCHEDULE_SETTING;
    }

    @Override
    public @NonNull String requestKeyName() {
        return "setting_id";
    }

    @Override
    public String servicePath() {
        return "/schedules";
    }

}
