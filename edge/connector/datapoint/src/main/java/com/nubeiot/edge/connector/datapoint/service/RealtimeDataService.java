package com.nubeiot.edge.connector.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.BigSerialKeyEntity;
import com.nubeiot.core.sql.ExtensionEntityService;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.edge.connector.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointRealtimeDataDao;
import com.nubeiot.iotdata.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.model.tables.records.PointRealtimeDataRecord;

import lombok.NonNull;

public final class RealtimeDataService
    extends AbstractDataPointService<Long, PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao>
    implements BigSerialKeyEntity<PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao>,
               ExtensionEntityService<Long, PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao>,
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

}
