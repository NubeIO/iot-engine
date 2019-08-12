package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.BigSerialKeyEntity;
import com.nubeiot.core.sql.HasReferenceEntityService;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.PointHistoryDataDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.records.PointHistoryDataRecord;

import lombok.NonNull;

public final class HistoryDataService
    extends AbstractDataPointService<Long, PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao>
    implements BigSerialKeyEntity<PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao>,
               HasReferenceEntityService<Long, PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao>,
               PointExtension {

    public HistoryDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "histories";
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

    @Override
    public @NonNull String requestKeyName() {
        return "history_id";
    }

    @Override
    public String servicePath() {
        return "/histories";
    }

}
