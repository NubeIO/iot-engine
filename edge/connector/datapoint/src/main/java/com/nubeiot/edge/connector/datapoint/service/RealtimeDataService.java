package com.nubeiot.edge.connector.datapoint.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.BigSerialKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.workflow.ConsumerService;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointRealtimeDataDao;
import com.nubeiot.iotdata.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.model.tables.records.PointRealtimeDataRecord;

import lombok.NonNull;

public final class RealtimeDataService
    extends DataPointService<Long, PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao>
    implements BigSerialKeyEntity<PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao> {

    public RealtimeDataService(@NonNull EntityHandler entityHandler, @NonNull HttpClientDelegate client) {
        super(entityHandler, client);
    }

    @Override
    protected @NonNull String listKey() {
        return "realtime_data";
    }

    @Override
    protected String endpoint() {
        return null;
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
