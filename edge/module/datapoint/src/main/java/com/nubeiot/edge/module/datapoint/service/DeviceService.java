package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DeviceService extends AbstractEntityService<Device, DeviceMetadata>
    implements DataPointService<Device, DeviceMetadata> {

    public DeviceService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public DeviceMetadata context() {
        return DeviceMetadata.INSTANCE;
    }

    public interface DeviceExtension extends HasReferenceResource {

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
