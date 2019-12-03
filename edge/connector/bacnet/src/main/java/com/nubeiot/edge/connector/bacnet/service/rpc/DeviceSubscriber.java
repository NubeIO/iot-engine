package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.rpc.AbstractSubscriber;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DeviceSubscriber extends AbstractSubscriber<Device> implements BACnetSubscriber<Device> {

    DeviceSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public Single<Device> create(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Device> update(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Device> patch(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Device> delete(RequestData requestData) {
        return null;
    }

}
