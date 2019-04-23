package com.nubeiot.core.http.client.handler;

import io.vertx.core.Handler;

import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientRegistry;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ClientEndHandler implements Handler<Void> {

    @NonNull
    private final HostInfo options;
    private final boolean isWebsocket;

    @Override
    public void handle(Void event) {
        HttpClientRegistry.getInstance().remove(options, isWebsocket);
    }

}
