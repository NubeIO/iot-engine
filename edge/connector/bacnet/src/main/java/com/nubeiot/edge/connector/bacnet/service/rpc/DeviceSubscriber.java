package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.rpc.AbstractProtocolSubscriber;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DeviceSubscriber extends AbstractProtocolSubscriber<Device> implements BACnetSubscriber<Device> {

    DeviceSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull EntityMetadata metadata() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public Single<Device> create(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public Single<Device> update(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public Single<Device> patch(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public Single<Device> delete(@NonNull RequestData requestData) {
        return null;
    }

}
