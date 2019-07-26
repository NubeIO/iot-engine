package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.UUIDKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.HistorySettingDao;
import com.nubeiot.iotdata.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.model.tables.records.HistorySettingRecord;

import lombok.NonNull;

public final class HistorySettingService
    extends AbstractDittoService<UUID, HistorySetting, HistorySettingRecord, HistorySettingDao>
    implements UUIDKeyModel<HistorySetting, HistorySettingRecord, HistorySettingDao> {

    public HistorySettingService(HistorySettingDao dao) {
        super(dao);
    }

    @Override
    protected @NonNull String listKey() {
        return "history_settings";
    }

    @Override
    public @NonNull String primaryKeyName() {
        return "point";
    }

    @Override
    public String endpoint() {
        return "/point/<point_code>/settings/history";
    }

    @Override
    public @NonNull Class<HistorySetting> model() {
        return HistorySetting.class;
    }

    @Override
    public @NonNull JsonTable<HistorySettingRecord> table() {
        return Tables.HISTORY_SETTING;
    }

}
