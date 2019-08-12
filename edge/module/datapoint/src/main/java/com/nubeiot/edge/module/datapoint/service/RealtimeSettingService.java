package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.HasReferenceEntityService;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.RealtimeSettingDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.edge.model.tables.records.RealtimeSettingRecord;

import lombok.NonNull;

public final class RealtimeSettingService
    extends AbstractDataPointService<UUID, RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao>
    implements UUIDKeyEntity<RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao>,
               HasReferenceEntityService<UUID, RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao>,
               PointExtension {

    public RealtimeSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "realtime_setting";
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
        return PointExtension.REQUEST_KEY;
    }

}
