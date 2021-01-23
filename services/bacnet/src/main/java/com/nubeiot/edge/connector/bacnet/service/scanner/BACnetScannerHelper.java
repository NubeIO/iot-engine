package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import lombok.NonNull;

public interface BACnetScannerHelper {

    static BACnetNetworkRpcScanner createNetworkScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        return new BACnetNetworkRpcScanner(sharedDataProxy);
    }

    static BACnetDeviceRpcScanner createDeviceScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        return new BACnetDeviceRpcScanner(sharedDataProxy);
    }

    static BACnetPointScanner createPointScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        return new BACnetPointScanner(sharedDataProxy);
    }

}
