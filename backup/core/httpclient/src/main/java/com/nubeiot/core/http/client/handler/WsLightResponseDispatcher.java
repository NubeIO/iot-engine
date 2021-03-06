package com.nubeiot.core.http.client.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.github.zero88.utils.Reflections.ReflectionClass;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.http.base.event.WebsocketServerEventMetadata;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Handle light Websocket response then dispatch based on Event Listener
 *
 * @see WebsocketServerEventMetadata
 */
@RequiredArgsConstructor
public abstract class WsLightResponseDispatcher implements Handler<Buffer> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NonNull
    private final EventbusClient controller;
    @NonNull
    private final EventModel listener;

    @SuppressWarnings("unchecked")
    public static <T extends WsLightResponseDispatcher> T create(@NonNull EventbusClient controller,
                                                                 @NonNull EventModel listener,
                                                                 @NonNull Class<T> bodyHandlerClass) {
        if (Objects.isNull(bodyHandlerClass) || WsLightResponseDispatcher.class.equals(bodyHandlerClass)) {
            return (T) new WsLightResponseDispatcher(controller, listener) {};
        }
        Map<Class, Object> params = new LinkedHashMap<>();
        params.put(EventbusClient.class, controller);
        params.put(EventModel.class, listener);
        return ReflectionClass.createObject(bodyHandlerClass, params);
    }

    @Override
    public void handle(Buffer data) {
        logger.info("Websocket Client received message then dispatch data to '{}'", listener.getAddress());
        controller.fire(listener.getAddress(), listener.getPattern(),
                        EventMessage.tryParse(JsonData.tryParse(data), true));
    }

}
