package com.nubeiot.edge.connector.bacnet.discover;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DiscoverLevel {

    NETWORK(1), DEVICE(2), OBJECT(3);

    private final int level;

    boolean mustValidate(@NonNull DiscoverLevel given) {
        return this.level <= given.level;
    }
}
