package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.HistorySettingDao;
import com.nubeiot.iotdata.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.model.tables.records.HistorySettingRecord;

import lombok.NonNull;

public final class HistorySettingService
    extends AbstractDittoService<UUID, HistorySetting, HistorySettingRecord, HistorySettingDao>
    implements UUIDKeyEntity<HistorySetting, HistorySettingRecord, HistorySettingDao> {

    public HistorySettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
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
    public @NonNull Class<HistorySetting> modelClass() {
        return HistorySetting.class;
    }

    @Override
    public @NonNull Class<HistorySettingDao> daoClass() {
        return HistorySettingDao.class;
    }

    @Override
    public @NonNull JsonTable<HistorySettingRecord> table() {
        return Tables.HISTORY_SETTING;
    }

}
