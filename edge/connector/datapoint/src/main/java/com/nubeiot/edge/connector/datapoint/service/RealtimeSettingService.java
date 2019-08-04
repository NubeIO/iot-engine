package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.ExtensionEntityService;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.edge.connector.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.RealtimeSettingDao;
import com.nubeiot.iotdata.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.model.tables.records.RealtimeSettingRecord;

import lombok.NonNull;

public final class RealtimeSettingService
    extends AbstractDataPointService<UUID, RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao>
    implements UUIDKeyEntity<RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao>,
               ExtensionEntityService<UUID, RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao>,
               PointExtension {

    public RealtimeSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "realtime_settings";
    }

    @Override
    public @NonNull Class<RealtimeSetting> modelClass() {
        return RealtimeSetting.class;
    }

    @Override
    public @NonNull Class<RealtimeSettingDao> daoClass() {
        return RealtimeSettingDao.class;
    }

    @Override
    public @NonNull JsonTable<RealtimeSettingRecord> table() {
        return Tables.REALTIME_SETTING;
    }

    @Override
    @NonNull
    public String requestKeyName() {
        return "point_id";
    }

}
