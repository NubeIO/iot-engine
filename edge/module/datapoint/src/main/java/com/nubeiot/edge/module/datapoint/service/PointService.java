package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.HasReferenceResource;
import com.nubeiot.core.sql.OneToManyReferenceEntityService;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.service.Metadata.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.Metadata.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.Metadata.PointMetadata;
import com.nubeiot.iotdata.edge.model.tables.daos.PointDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.records.PointRecord;

import lombok.NonNull;

public final class PointService extends AbstractDataPointService<UUID, Point, PointRecord, PointDao, PointMetadata>
    implements OneToManyReferenceEntityService<UUID, Point, PointRecord, PointDao, PointMetadata> {

    public PointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public RequestData recompute(RequestData requestData, Map<String, ?> extra) {
        Metadata.NetworkMetadata.optimizeAlias(requestData.body());
        Metadata.NetworkMetadata.optimizeAlias(requestData.getFilter());
        return OneToManyReferenceEntityService.super.recompute(requestData, extra);
    }

    @Override
    public Map<String, String> jsonRefFields() {
        final Map<String, String> jsonRefFields = new HashMap<>();
        jsonRefFields.put(DeviceMetadata.INSTANCE.requestKeyName(), metadata().table().DEVICE.getName());
        jsonRefFields.put(NetworkMetadata.INSTANCE.requestKeyName(), metadata().table().NETWORK.getName());
        return jsonRefFields;
    }

    @Override
    public Map<String, Function<String, ?>> jsonFieldConverter() {
        final Map<String, Function<String, ?>> extensions = new HashMap<>();
        extensions.put(DeviceMetadata.INSTANCE.requestKeyName(), Functions.toUUID());
        extensions.put(NetworkMetadata.INSTANCE.requestKeyName(), Functions.toUUID());
        return extensions;
    }

    @Override
    public PointMetadata metadata() {
        return PointMetadata.INSTANCE;
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
