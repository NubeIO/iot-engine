package com.nubeiot.edge.connector.bacnet.service;

import io.vertx.core.Vertx;

import lombok.NonNull;

public final class DeviceDiscovery extends AbstractBACnetDiscoveryService implements BACnetDiscoveryService {

    DeviceDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull String servicePath() {
        return "/discovery/bacnet/network/:network_name/device";
    }

    @Override
    public String paramPath() {
        return "/:device_code";
    }

}
