package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.UUIDKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.ScheduleSettingDao;
import com.nubeiot.iotdata.model.tables.pojos.ScheduleSetting;
import com.nubeiot.iotdata.model.tables.records.ScheduleSettingRecord;

import lombok.NonNull;

public final class ScheduleSettingService
    extends AbstractDittoService<UUID, ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao>
    implements UUIDKeyModel<ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao> {

    public ScheduleSettingService(ScheduleSettingDao dao) {
        super(dao);
    }

    @Override
    protected ScheduleSetting parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return new ScheduleSetting(request);
    }

    @Override
    protected @NonNull String listKey() {
        return "schedules";
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
