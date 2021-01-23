package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import lombok.NonNull;

public interface BACnetScannerHelper {

    static BACnetNetworkScanner createNetworkScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetNetworkScanner(vertx, sharedKey);
    }

    static BACnetDeviceScanner createDeviceScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetDeviceScanner(vertx, sharedKey);
    }

    static BACnetPointScanner createPointScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        return new BACnetPointScanner(vertx, sharedKey);
    }

}
