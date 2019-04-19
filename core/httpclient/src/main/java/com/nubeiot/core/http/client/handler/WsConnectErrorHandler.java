package com.nubeiot.core.http.client.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.TimeoutException;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class WsConnectErrorHandler implements Handler<Throwable> {

    private final EventController controller;

    @SuppressWarnings("unchecked")
    public static <T extends WsConnectErrorHandler> T create(@NonNull EventController controller,
                                                             @NonNull Class<T> connErrorHandlerClass) {
        Map<Class, Object> params = new LinkedHashMap<>();
        params.put(EventController.class, controller);
        return Objects.isNull(connErrorHandlerClass) || WsConnectErrorHandler.class.equals(connErrorHandlerClass)
               ? (T) new WsConnectErrorHandler(controller) {}
               : ReflectionClass.createObject(connErrorHandlerClass, params);
    }

    @Override
    public void handle(Throwable error) {
        if (error instanceof VertxException && error.getMessage().equals("Connection was closed")) {
            throw new TimeoutException("Request timeout", error);
        }
        throw new HttpException("Failed when open websocket connection", error);
    }

}
