package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.service.Metadata.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.Metadata.NetworkMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class NetworkService extends AbstractDataPointService<Network, NetworkMetadata, NetworkService>
    implements OneToManyReferenceEntityService<Network, NetworkMetadata, NetworkService>, ReferenceEntityTransformer {

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

    @Override
    public HasReferenceResource ref() {
        return this;
    }

    @Override
    public @NonNull ReferenceEntityTransformer transformer() {
        return this;
    }

    @Override
    public @NonNull ReferenceQueryExecutor<Network> queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
