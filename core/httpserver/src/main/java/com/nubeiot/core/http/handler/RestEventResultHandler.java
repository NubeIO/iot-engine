package com.nubeiot.core.http.handler;

import java.util.LinkedHashMap;
import java.util.Objects;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.utils.RequestDataConverter;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents for pushing data via {@code Eventbus} then listen {@code reply message}. After receiving {@code reply
 * message}, redirect it to {@code next Context handler}
 *
 * @see RestEventResponseHandler
 */
@RequiredArgsConstructor
public class RestEventResultHandler implements EventResultContextHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter
    @NonNull
    private final EventController controller;
    @NonNull
    private final String address;
    @NonNull
    private final EventAction action;
    @NonNull
    private final EventPattern pattern;

    @SuppressWarnings("unchecked")
    public static <T extends RestEventResultHandler> RestEventResultHandler create(Class<T> handler,
                                                                                   EventController eventController,
                                                                                   String address, EventAction action,
                                                                                   EventPattern pattern) {
        Class<T> handlerClass = Objects.isNull(handler) ? (Class<T>) RestEventResultHandler.class : handler;
        LinkedHashMap<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(EventController.class, eventController);
        inputs.put(String.class, Strings.requireNotBlank(address));
        inputs.put(EventAction.class, action);
        inputs.put(EventPattern.class, pattern);
        return ReflectionClass.createObject(handlerClass, inputs);
    }

    @Override
    public void handle(RoutingContext context) {
        EventMessage msg = EventMessage.initial(action, RequestDataConverter.convert(context));
        logger.info("REST::Request data: {}", msg.toJson().encode());
        sendAndListenEvent(context, "REST", address, pattern, msg);
    }

}
