package com.nubeiot.edge.connector.bacnet;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class BacnetConfig extends AbstractBACnetConfig {

    @NonNull
    private String gatewayAddress;

    @Override
    protected int maxDeviceId() {
        return MAX_DEVICE_ID;
    }

    @Override
    protected int minDeviceId() {
        return MIN_DEVICE_ID;
    }

}
