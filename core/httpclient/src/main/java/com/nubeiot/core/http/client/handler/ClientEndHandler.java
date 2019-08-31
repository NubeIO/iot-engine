package com.nubeiot.core.http.client.handler;

import io.reactivex.functions.Action;
import io.vertx.core.Handler;

import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientRegistry;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ClientEndHandler implements Handler<Void>, Action {

    @NonNull
    private final HostInfo options;
    private final boolean isWebsocket;

    @Override
    public void handle(Void event) {
        HttpClientRegistry.getInstance().remove(options, isWebsocket);
    }

    @Override
    public void run() {
        HttpClientRegistry.getInstance().remove(options, isWebsocket);
    }

}
