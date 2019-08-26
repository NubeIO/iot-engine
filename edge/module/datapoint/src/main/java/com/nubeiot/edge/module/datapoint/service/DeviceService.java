package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
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
        default EntityReferences entityReferences() {
            return new EntityReferences().add(DeviceMetadata.INSTANCE, "device");
        }

    }

}
