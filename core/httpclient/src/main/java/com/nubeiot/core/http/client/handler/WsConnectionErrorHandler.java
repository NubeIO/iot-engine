package com.nubeiot.core.http.client.handler;

import io.vertx.core.Handler;

import com.nubeiot.core.event.EventController;

import lombok.NonNull;

public abstract class WsConnectionErrorHandler implements Handler<Throwable> {

    public static <T extends WsConnectionErrorHandler> T create(@NonNull EventController controller,
                                                                @NonNull Class<T> connErrorHandlerClass) {
        return null;
    }

    @Override
    public void handle(Throwable event) {

    }

}
