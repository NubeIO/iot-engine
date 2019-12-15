package com.nubeiot.edge.connector.bacnet;

public final class MockBACnetConfig extends AbstractBACnetConfig {

    @Override
    protected int maxDeviceId() {
        return MAX_DEVICE_ID;
    }

    @Override
    protected int minDeviceId() {
        return MIN_DEVICE_ID;
    }

}
