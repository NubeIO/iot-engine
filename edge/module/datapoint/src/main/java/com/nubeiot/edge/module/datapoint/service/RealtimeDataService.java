package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.BigSerialKeyEntity;
import com.nubeiot.core.sql.HasReferenceEntityService;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.PointRealtimeDataDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.records.PointRealtimeDataRecord;

import lombok.NonNull;

public final class RealtimeDataService
    extends AbstractDataPointService<Long, PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao>
    implements BigSerialKeyEntity<PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao>,
               HasReferenceEntityService<Long, PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao>,
               PointExtension {

    public RealtimeDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "realtime_data";
    }

    @Override
    public @NonNull Class<PointRealtimeData> modelClass() {
        return PointRealtimeData.class;
    }

    @Override
    public @NonNull Class<PointRealtimeDataDao> daoClass() {
        return PointRealtimeDataDao.class;
    }

    @Override
    public @NonNull JsonTable<PointRealtimeDataRecord> table() {
        return Tables.POINT_REALTIME_DATA;
    }

    @Override
    public @NonNull String requestKeyName() {
        return "realtime_id";
    }

    @Override
    public String servicePath() {
        return "realtime-data";
    }

}
