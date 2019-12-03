package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.rpc.AbstractSubscriber;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class NetworkSubscriber extends AbstractSubscriber<Network> implements BACnetSubscriber<Network> {

    NetworkSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public Single<Network> create(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Network> update(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Network> patch(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Network> delete(RequestData requestData) {
        return null;
    }

}
