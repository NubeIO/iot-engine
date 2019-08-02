package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointDao;
import com.nubeiot.iotdata.model.tables.pojos.Point;
import com.nubeiot.iotdata.model.tables.records.PointRecord;

import lombok.NonNull;

public final class PointService extends DataPointService<UUID, Point, PointRecord, PointDao>
    implements UUIDKeyEntity<Point, PointRecord, PointDao> {

    public PointService(@NonNull EntityHandler entityHandler, @NonNull HttpClientDelegate client) {
        super(entityHandler, client);
    }

    @Override
    protected @NonNull String listKey() {
        return "points";
    }

    @Override
    public String endpoint() {
        return "/point";
    }

    @Override
    public @NonNull Class<Point> modelClass() {
        return Point.class;
    }

    @Override
    public @NonNull Class<PointDao> daoClass() {
        return PointDao.class;
    }

    @Override
    public @NonNull JsonTable<PointRecord> table() {
        return Tables.POINT;
    }

    @Override
    public Point parse(JsonObject request) {
        return new Point(request);
    }

}
