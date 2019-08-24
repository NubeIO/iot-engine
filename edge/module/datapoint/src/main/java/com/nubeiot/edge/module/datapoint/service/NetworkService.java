package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.NetworkMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class NetworkService extends AbstractOneToManyEntityService<Network, NetworkMetadata>
    implements DataPointService<Network, NetworkMetadata> {

    public NetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public Map<String, String> jsonRefFields() {
        return Collections.singletonMap(DeviceMetadata.INSTANCE.requestKeyName(), context().table().DEVICE.getName());
    }

    @Override
    public Map<String, Function<String, ?>> jsonFieldConverter() {
        return Collections.singletonMap(DeviceMetadata.INSTANCE.requestKeyName(), Functions.toUUID());
    }

    @Override
    public NetworkMetadata context() {
        return NetworkMetadata.INSTANCE;
    }

}
