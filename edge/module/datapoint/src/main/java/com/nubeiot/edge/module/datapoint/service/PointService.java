package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.HasReferenceEntityService;
import com.nubeiot.core.sql.HasReferenceResource;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.PointDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.records.PointRecord;

import lombok.NonNull;

public final class PointService extends AbstractDataPointService<UUID, Point, PointRecord, PointDao>
    implements UUIDKeyEntity<Point, PointRecord, PointDao>,
               HasReferenceEntityService<UUID, Point, PointRecord, PointDao> {

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
    public @NonNull com.nubeiot.iotdata.edge.model.tables.Point table() {
        return Tables.POINT;
    }

    @Override
    public Point parse(JsonObject request) {
        return new Point(request);
    }

    @Override
    public RequestData recompute(RequestData requestData, Map<String, ?> extra) {
        NetworkService.optimizeAlias(requestData.body());
        NetworkService.optimizeAlias(requestData.getFilter());
        return HasReferenceEntityService.super.recompute(requestData, extra);
    }

    @Override
    public Map<String, String> jsonRefFields() {
        final Map<String, String> jsonRefFields = new HashMap<>();
        jsonRefFields.put(DeviceService.REQUEST_KEY, table().DEVICE.getName());
        jsonRefFields.put(NetworkService.REQUEST_KEY, table().NETWORK.getName());
        return jsonRefFields;
    }

    @Override
    public Map<String, Function<String, ?>> jsonFieldConverter() {
        final Map<String, Function<String, ?>> extensions = new HashMap<>();
        extensions.put(DeviceService.REQUEST_KEY, Functions.toUUID());
        extensions.put(NetworkService.REQUEST_KEY, Functions.toUUID());
        return extensions;
    }

    public interface PointExtension extends HasReferenceResource {

        String REQUEST_KEY = EntityService.createRequestKeyName(Point.class, Tables.POINT.ID.getName());

        @Override
        default Map<String, String> jsonRefFields() {
            return Collections.singletonMap(REQUEST_KEY, "point");
        }

        @Override
        default Map<String, Function<String, ?>> jsonFieldConverter() {
            return Collections.singletonMap(REQUEST_KEY, Functions.toUUID());
        }

    }

}
