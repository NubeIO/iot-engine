package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.OneToManyReferenceEntityService;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.service.Metadata.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.Metadata.NetworkMetadata;
import com.nubeiot.iotdata.edge.model.tables.daos.NetworkDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.edge.model.tables.records.NetworkRecord;

import lombok.NonNull;

public final class NetworkService
    extends AbstractDataPointService<UUID, Network, NetworkRecord, NetworkDao, NetworkMetadata>
    implements OneToManyReferenceEntityService<UUID, Network, NetworkRecord, NetworkDao, NetworkMetadata> {

    public NetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public Map<String, String> jsonRefFields() {
        return Collections.singletonMap(DeviceMetadata.INSTANCE.requestKeyName(), metadata().table().DEVICE.getName());
    }

    @Override
    public Map<String, Function<String, ?>> jsonFieldConverter() {
        return Collections.singletonMap(DeviceMetadata.INSTANCE.requestKeyName(), Functions.toUUID());
    }

    @Override
    public NetworkMetadata metadata() {
        return NetworkMetadata.INSTANCE;
    }

}
