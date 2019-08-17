package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.service.Metadata.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.Metadata.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.Metadata.PointMetadata;

import lombok.NonNull;

public final class PointService extends AbstractDataPointService<PointMetadata, PointService>
    implements OneToManyReferenceEntityService<PointMetadata, PointService>, ReferenceEntityTransformer {

    public PointService(@NonNull AbstractEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public RequestData recompute(RequestData requestData, JsonObject extra) {
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

    @Override
    public HasReferenceResource ref() {
        return this;
    }

    @Override
    public @NonNull OneToManyReferenceEntityService.ReferenceEntityTransformer transformer() {
        return this;
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
