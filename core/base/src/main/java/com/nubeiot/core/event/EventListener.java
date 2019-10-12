package com.nubeiot.core.event;

import java.util.Collection;
import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.DesiredException;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeExceptionConverter;

import lombok.NonNull;

/**
 * Handlers a received {@code Eventbus} message.
 *
 * @see EventContractor
 * @see EventMessage
 * @see EventAction
 * @see EventPattern#REQUEST_RESPONSE
 */
public interface EventListener extends Function<Message<Object>, Single<EventMessage>> {

    /**
     * Available events that this handler can process
     *
     * @return list of possible events
     */
    @NonNull Collection<EventAction> getAvailableEvents();

    /**
     * Jackson Object mapper for serialize/deserialize data
     *
     * @return Object mapper. Default: {@link JsonData#MAPPER}
     */
    default ObjectMapper mapper() { return JsonData.MAPPER; }

    /**
     * Fallback json key if output is {@code collection/primitive}  value
     *
     * @return fallback json key. Default: {@code data}
     */
    default String fallback() { return "data"; }

    @Override
    default Single<EventMessage> apply(Message<Object> message) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        EventMessage msg = EventMessage.tryParse(message.body());
        EventAction action = msg.getAction();
        AnnotationHandler<? extends EventListener> handler = new AnnotationHandler<>(this);
        return handler.execute(msg)
                      .map(data -> EventMessage.success(action, data))
                      .onErrorReturn(t -> error(action, logger).apply(t))
                      .doOnSuccess(data -> message.reply(data.toJson()));
    }

    default Function<Throwable, EventMessage> error(EventAction action, Logger logger) {
        return throwable -> {
            if (throwable instanceof DesiredException) {
                logger.debug("Failed when handle event {}", throwable, action);
            } else {
                logger.error("Failed when handle event {}", throwable, action);
            }
            Throwable t = NubeExceptionConverter.friendly(throwable, throwable instanceof ImplementationError ?
                                                                     "No reply from event " + action : null);
            return EventMessage.error(action, t);
        };
    }

}
