package com.nubeiot.edge.module.datapoint.task.remote;

import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.micro.discovery.RemoteServiceInvoker;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class ProtocolDispatcherRpcClient implements RemoteServiceInvoker {

    @NonNull
    private final EventbusClient client;

    @Override
    public String requester() {
        return "datapoint";
    }

    @Override
    public @NonNull EventbusClient transporter() {
        return client;
    }

    @Override
    public String serviceLabel() {
        return "Data Protocol subscriber";
    }

}
