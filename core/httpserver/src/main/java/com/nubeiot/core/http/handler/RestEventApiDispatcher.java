package com.nubeiot.core.http.handler;

import java.util.LinkedHashMap;
import java.util.Objects;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.http.converter.RequestDataConverter;
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
public class RestEventApiDispatcher implements RestEventRequestDispatcher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter
    @NonNull
    private final EventbusClient controller;
    @NonNull
    private final String address;
    @NonNull
    private final EventAction action;
    @NonNull
    private final EventPattern pattern;
    private final boolean useRequestData;

    @SuppressWarnings("unchecked")
    public static <T extends RestEventApiDispatcher> RestEventApiDispatcher create(Class<T> handler,
                                                                                   EventbusClient eventbusClient,
                                                                                   String address, EventAction action,
                                                                                   EventPattern pattern,
                                                                                   boolean useRequestData) {
        Class<T> handlerClass = Objects.isNull(handler) ? (Class<T>) RestEventApiDispatcher.class : handler;
        LinkedHashMap<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(EventbusClient.class, eventbusClient);
        inputs.put(String.class, Strings.requireNotBlank(address));
        inputs.put(EventAction.class, action);
        inputs.put(EventPattern.class, pattern);
        inputs.put(boolean.class, useRequestData);
        return ReflectionClass.createObject(handlerClass, inputs);
    }

    @Override
    public void handle(RoutingContext context) {
        EventMessage msg = useRequestData
                           ? EventMessage.initial(action, RequestDataConverter.convert(context))
                           : EventMessage.initial(action, RequestDataConverter.body(context));
        logger.info("REST::Dispatch data to Event Address {}", address);
        dispatch(context, "REST", address, pattern, msg);
    }

}
