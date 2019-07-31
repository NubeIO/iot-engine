package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.workflow.ConsumerService;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.ScheduleSettingDao;
import com.nubeiot.iotdata.model.tables.pojos.ScheduleSetting;
import com.nubeiot.iotdata.model.tables.records.ScheduleSettingRecord;

import lombok.NonNull;

public final class ScheduleSettingService
    extends DataPointService<UUID, ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao>
    implements UUIDKeyEntity<ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao> {

    public ScheduleSettingService(@NonNull EntityHandler entityHandler, @NonNull HttpClientDelegate client) {
        super(entityHandler, client);
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
