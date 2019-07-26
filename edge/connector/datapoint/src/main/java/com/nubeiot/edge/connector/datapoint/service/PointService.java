package com.nubeiot.edge.connector.datapoint.service;

import java.util.List;
import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.UUIDKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointDao;
import com.nubeiot.iotdata.model.tables.pojos.Point;
import com.nubeiot.iotdata.model.tables.records.PointRecord;

import lombok.NonNull;

public final class PointService extends AbstractDittoService<UUID, Point, PointRecord, PointDao>
    implements CompositeDittoService, UUIDKeyModel<Point, PointRecord, PointDao> {

    public PointService(PointDao dao) {
        super(dao);
    }

    @Override
    public String endpoint() {
        return "/point";
    }

    @Override
    public @NonNull JsonTable<PointRecord> table() {
        return Tables.POINT;
    }

    @Override
    protected Point parse(JsonObject request) {
        return new Point(request);
    }

    @Override
    protected @NonNull String listKey() {
        return "points";
    }

    @Override
    public List<DittoService> children() {
        return null;
    }

}
