package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import lombok.NonNull;

public interface BACnetScannerHelper {

    static BACnetNetworkScanner createNetworkScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        return new BACnetNetworkScanner(sharedDataProxy);
    }

    static BACnetDeviceScanner createDeviceScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        return new BACnetDeviceScanner(sharedDataProxy);
    }

    static BACnetPointScanner createPointScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        return new BACnetPointScanner(sharedDataProxy);
    }

}
