package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.DeviceService.DeviceExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class NetworkService extends AbstractOneToManyEntityService<Network, NetworkMetadata>
    implements DataPointService<Network, NetworkMetadata>, DeviceExtension {

    public NetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public NetworkMetadata context() {
        return NetworkMetadata.INSTANCE;
    }

}
