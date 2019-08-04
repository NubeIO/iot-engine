package com.nubeiot.edge.connector.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.ExtensionEntityService;
import com.nubeiot.core.sql.ExtensionResource;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointDao;
import com.nubeiot.iotdata.model.tables.pojos.Network;
import com.nubeiot.iotdata.model.tables.pojos.Point;
import com.nubeiot.iotdata.model.tables.records.PointRecord;

import lombok.NonNull;

public final class PointService extends AbstractDataPointService<UUID, Point, PointRecord, PointDao>
    implements UUIDKeyEntity<Point, PointRecord, PointDao>, ExtensionEntityService<UUID, Point, PointRecord, PointDao> {

    public PointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "points";
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

    @Override
    public Map<String, Function<String, ?>> extensions() {
        return Collections.singletonMap(EntityService.createRequestKeyName(Network.class, Tables.NETWORK.ID.getName()),
                                        Functions.toUUID());
    }

    public interface PointExtension extends ExtensionResource {

        @Override
        default Map<String, Function<String, ?>> extensions() {
            return Collections.singletonMap(EntityService.createRequestKeyName(Point.class, Tables.POINT.ID.getName()),
                                            Functions.toUUID());
        }

    }

}
