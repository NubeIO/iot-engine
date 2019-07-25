package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import org.jooq.Table;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.AbstractModelService;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.HistorySettingDao;
import com.nubeiot.iotdata.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.model.tables.records.HistorySettingRecord;

import lombok.NonNull;

public final class HistorySettingService
    extends AbstractModelService<UUID, HistorySetting, HistorySettingRecord, HistorySettingDao>
    implements DittoService {

    public HistorySettingService(HistorySettingDao dao) {
        super(dao);
    }

    @Override
    public String endpoint() {
        return "/point/<point_code>/settings/history";
    }

    @Override
    protected HistorySetting parse(JsonObject object) {
        return null;
    }

    @Override
    protected @NonNull Table<HistorySettingRecord> table() {
        return Tables.HISTORY_SETTING;
    }

    @Override
    protected @NonNull String idKey() {
        return "point";
    }

    @Override
    protected UUID id(String requestKey) throws IllegalArgumentException {
        return UUID.fromString(requestKey);
    }

    @Override
    protected boolean hasTimeAudit() { return true; }

    @Override
    protected @NonNull String listKey() {
        return "history_settings";
    }

    @Override
    protected HistorySetting validateOnCreate(HistorySetting pojo) throws IllegalArgumentException {
        return null;
    }

    @Override
    protected HistorySetting validateOnUpdate(HistorySetting pojo) throws IllegalArgumentException {
        return null;
    }

    @Override
    protected HistorySetting validateOnPatch(HistorySetting pojo) throws IllegalArgumentException {
        return null;
    }

}
