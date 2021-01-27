package com.nubeiot.edge.connector.bacnet.discovery;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DiscoveryLevel {

    NETWORK(1), DEVICE(2), OBJECT(3);

    private final int level;

    boolean mustValidate(@NonNull DiscoveryLevel given) {
        return this.level <= given.level;
    }
}
