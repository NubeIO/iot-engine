package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.ScheduleSettingDao;
import com.nubeiot.iotdata.model.tables.pojos.ScheduleSetting;
import com.nubeiot.iotdata.model.tables.records.ScheduleSettingRecord;

import lombok.NonNull;

public final class ScheduleSettingService
    extends AbstractDittoService<UUID, ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao>
    implements UUIDKeyEntity<ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao> {

    public ScheduleSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull String listKey() {
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
    public String endpoint() {
        return null;
    }

}
