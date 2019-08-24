package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.NonNull;

public final class PointService extends AbstractOneToManyEntityService<Point, PointMetadata>
    implements DataPointService<Point, PointMetadata> {

    public PointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public PointMetadata context() {
        return PointMetadata.INSTANCE;
    }

    @Override
    public RequestData recompute(RequestData requestData, JsonObject extra) {
        DataPointIndex.NetworkMetadata.optimizeAlias(requestData.body());
        DataPointIndex.NetworkMetadata.optimizeAlias(requestData.getFilter());
        return super.recompute(requestData, extra);
    }

    @Override
    public Map<String, String> jsonRefFields() {
        final Map<String, String> jsonRefFields = new HashMap<>();
        jsonRefFields.put(DeviceMetadata.INSTANCE.requestKeyName(), context().table().DEVICE.getName());
        jsonRefFields.put(NetworkMetadata.INSTANCE.requestKeyName(), context().table().NETWORK.getName());
        return jsonRefFields;
    }

    @Override
    public Map<String, Function<String, ?>> jsonFieldConverter() {
        final Map<String, Function<String, ?>> extensions = new HashMap<>();
        extensions.put(context().requestKeyName(), Functions.toUUID());
        extensions.put(DeviceMetadata.INSTANCE.requestKeyName(), Functions.toUUID());
        extensions.put(NetworkMetadata.INSTANCE.requestKeyName(), Functions.toUUID());
        return extensions;
    }

    public interface PointExtension extends HasReferenceResource {

        @Override
        default Map<String, String> jsonRefFields() {
            return Collections.singletonMap(PointMetadata.INSTANCE.requestKeyName(), "point");
        }

        @Override
        default Map<String, Function<String, ?>> jsonFieldConverter() {
            return Collections.singletonMap(PointMetadata.INSTANCE.requestKeyName(), Functions.toUUID());
        }

    }

}
