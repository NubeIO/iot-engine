package com.nubeiot.edge.connector.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.BigSerialKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointHistoryDataDao;
import com.nubeiot.iotdata.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.model.tables.records.PointHistoryDataRecord;

import lombok.NonNull;

public final class HistoryDataService
    extends AbstractDittoService<Long, PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao>
    implements BigSerialKeyEntity<PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao> {

    public HistoryDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull String listKey() {
        return "histories";
    }

    @Override
    public String endpoint() {
        return null;
    }

    @Override
    public @NonNull Class<PointHistoryData> modelClass() {
        return PointHistoryData.class;
    }

    @Override
    public @NonNull Class<PointHistoryDataDao> daoClass() {
        return PointHistoryDataDao.class;
    }

    @Override
    public @NonNull JsonTable<PointHistoryDataRecord> table() {
        return Tables.POINT_HISTORY_DATA;
    }

}
