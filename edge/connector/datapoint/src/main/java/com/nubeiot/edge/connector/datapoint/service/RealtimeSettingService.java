package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.UUIDKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.RealtimeSettingDao;
import com.nubeiot.iotdata.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.model.tables.records.RealtimeSettingRecord;

import lombok.NonNull;

public final class RealtimeSettingService
    extends AbstractDittoService<UUID, RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao>
    implements UUIDKeyModel<RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao> {

    public RealtimeSettingService(RealtimeSettingDao dao) {
        super(dao);
    }

    @Override
    protected @NonNull String listKey() {
        return "realtime_settings";
    }

    @Override
    protected @NonNull String primaryKeyName() {
        return "point";
    }

    @Override
    public @NonNull Class<RealtimeSetting> model() {
        return RealtimeSetting.class;
    }

    @Override
    public @NonNull JsonTable<RealtimeSettingRecord> table() {
        return Tables.REALTIME_SETTING;
    }

    @Override
    public String endpoint() {
        return "/point";
    }

}
