package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import lombok.NonNull;

public interface BACnetScannerHelper {

    static BACnetNetworkRpcScanner createNetworkScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetNetworkRpcScanner(vertx, sharedKey);
    }

    static BACnetDeviceRpcScanner createDeviceScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetDeviceRpcScanner(vertx, sharedKey);
    }

    static BACnetPointScanner createPointScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetPointScanner(vertx, sharedKey);
    }

}
