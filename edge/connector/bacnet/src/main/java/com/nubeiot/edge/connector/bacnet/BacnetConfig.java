package com.nubeiot.edge.connector.bacnet;

import lombok.Getter;

@Getter
public final class BacnetConfig extends AbstractBACnetConfig {

    private String gatewayDiscoverAddress;

    @Override
    protected int maxDeviceId() {
        return MAX_DEVICE_ID;
    }

    @Override
    protected int minDeviceId() {
        return MIN_DEVICE_ID;
    }

}
