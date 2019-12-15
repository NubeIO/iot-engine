package com.nubeiot.core.micro.discovery.mock;

import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.micro.discovery.GatewayServiceInvoker;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MockServiceInvoker implements GatewayServiceInvoker {

    private final String gatewayAddress;
    private final EventbusClient client;
    private final String destination;

    @Override
    public @NonNull String gatewayAddress() {
        return gatewayAddress;
    }

    @Override
    public String requester() {
        return "discovery.test";
    }

    @Override
    public @NonNull EventbusClient eventClient() {
        return client;
    }

    @Override
    public String serviceLabel() {
        return "Mock Service";
    }

    @Override
    public @NonNull String destination() {
        return destination;
    }

}
