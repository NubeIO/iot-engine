package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import org.jooq.Table;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.AbstractModelService;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointDao;
import com.nubeiot.iotdata.model.tables.pojos.Point;
import com.nubeiot.iotdata.model.tables.records.PointRecord;

import lombok.NonNull;

public final class PointService extends AbstractModelService<UUID, Point, PointRecord, PointDao>
    implements DittoService {

    public PointService(PointDao dao) {
        super(dao);
    }

    @Override
    public String endpoint() {
        return "/point";
    }

    @Override
    protected Point parse(JsonObject object) {
        return new Point(object);
    }

    @Override
    protected @NonNull Table<PointRecord> table() {
        return Tables.POINT;
    }

    @Override
    protected UUID id(String requestKey) throws IllegalArgumentException {
        return UUID.fromString(requestKey);
    }

    @Override
    public boolean hasTimeAudit() { return true; }

    @Override
    protected @NonNull String listKey() {
        return "points";
    }

    @Override
    protected Point validateOnCreate(Point pojo) throws IllegalArgumentException {
        return pojo;
    }

    @Override
    protected Point validateOnUpdate(Point pojo) throws IllegalArgumentException {
        return pojo;
    }

    @Override
    protected Point validateOnPatch(Point pojo) throws IllegalArgumentException {
        return pojo;
    }

}
