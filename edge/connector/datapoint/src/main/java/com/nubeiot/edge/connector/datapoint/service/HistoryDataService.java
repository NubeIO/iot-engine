package com.nubeiot.edge.connector.datapoint.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.BigSerialKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointHistoryDataDao;
import com.nubeiot.iotdata.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.model.tables.records.PointHistoryDataRecord;

import lombok.NonNull;

public final class HistoryDataService
    extends AbstractDittoService<Long, PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao>
    implements BigSerialKeyModel<PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao> {

    public HistoryDataService(PointHistoryDataDao dao) {
        super(dao);
    }

    @Override
    protected PointHistoryData parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return new PointHistoryData(request);
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
    public @NonNull JsonTable<PointHistoryDataRecord> table() {
        return Tables.POINT_HISTORY_DATA;
    }

}
