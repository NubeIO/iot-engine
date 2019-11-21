package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeComposite;
import com.nubeiot.iotdata.edge.model.tables.EdgeDevice;

import lombok.NonNull;

public final class DeviceByEdgeService
    extends AbstractManyToManyEntityService<EdgeComposite, EdgeDeviceCompositeMetadata>
    implements DataPointService<EdgeComposite, EdgeDeviceCompositeMetadata> {

    public DeviceByEdgeService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EdgeDeviceCompositeMetadata context() {
        return EdgeDeviceCompositeMetadata.INSTANCE;
    }

    @Override
    public EntityReferences entityReferences() {
        final @NonNull EdgeDevice table = context().table();
        return new EntityReferences().add(reference(), table.getJsonField(table.EDGE_ID))
                                     .add(resource(), table.getJsonField(table.DEVICE_ID));
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return EdgeMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public Set<String> ignoreFields(@NonNull RequestData requestData) {
        final Set<String> ignores = super.ignoreFields(requestData);
        ignores.add(context().table().getJsonField(context().table().DEVICE_ID));
        return ignores;
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return DataPointService.definitionsForMany(getAvailableEvents(), reference(), resource());
    }

}
