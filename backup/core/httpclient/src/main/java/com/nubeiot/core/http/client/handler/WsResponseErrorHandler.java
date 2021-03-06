package com.nubeiot.core.http.client.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.github.zero88.utils.Reflections.ReflectionClass;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventbusClient;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class WsResponseErrorHandler implements Handler<Throwable> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @NonNull
    private final EventbusClient controller;
    @NonNull
    private final EventModel listener;

    @SuppressWarnings("unchecked")
    public static <T extends WsResponseErrorHandler> T create(@NonNull EventbusClient controller,
                                                              @NonNull EventModel listener,
                                                              @NonNull Class<T> errorHandlerClass) {
        if (Objects.isNull(errorHandlerClass) || WsResponseErrorHandler.class.equals(errorHandlerClass)) {
            return (T) new IgnoreWsResponseError(controller, listener) {};
        }
        Map<Class, Object> params = new LinkedHashMap<>();
        params.put(EventbusClient.class, controller);
        params.put(EventModel.class, listener);
        return ReflectionClass.createObject(errorHandlerClass, params);
    }

    public static class IgnoreWsResponseError extends WsResponseErrorHandler {

        IgnoreWsResponseError(@NonNull EventbusClient controller, @NonNull EventModel listener) {
            super(controller, listener);
        }

        @Override
        public void handle(Throwable error) {
            this.logger.warn("Error in websocket response", error);
        }

    }

}
