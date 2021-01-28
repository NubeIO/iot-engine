package com.nubeiot.edge.connector.bacnet.service.coordinator;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.service.InboundBACnetCoordinator;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

public final class BACnetCovCoordinator implements InboundBACnetCoordinator<IoTEntity> {

    @Override
    public @NonNull Class<IoTEntity> context() {
        return null;
    }

    @Override
    public @NonNull SharedDataLocalProxy sharedData() {
        return null;
    }

    @Override
    public @NonNull String destination() {
        return null;
    }

    @Override
    public Single<JsonObject> register() {
        return null;
    }

    @Override
    public String publishAddress() {
        return null;
    }

}
