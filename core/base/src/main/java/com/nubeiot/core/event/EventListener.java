package com.nubeiot.core.event;

import java.util.Collection;
import java.util.function.Consumer;

import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.DesiredException;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Handlers a received {@code Eventbus} message.
 *
 * @see EventContractor
 * @see EventMessage
 * @see EventAction
 * @see EventPattern#REQUEST_RESPONSE
 */
public interface EventListener extends Consumer<Message<Object>> {

    /**
     * Available events that this handler can process
     *
     * @return list of possible events
     */
    @NonNull Collection<EventAction> getAvailableEvents();

    @SuppressWarnings("unchecked")
    default void accept(io.vertx.reactivex.core.eventbus.Message<Object> message) {
        this.accept(message.getDelegate());
    }

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
    default void accept(Message<Object> message) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        EventMessage msg = EventMessage.tryParse(message.body());
        EventAction action = msg.getAction();
        AnnotationHandler<? extends EventListener> handler = new AnnotationHandler<>(this);
        try {
            handler.execute(msg)
                   .subscribe(data -> message.reply(EventMessage.success(action, data).toJson()),
                              error(message, action, logger, null)::accept);
        } catch (ImplementationError ex) {
            error(message, action, logger, "No reply from event " + action).accept(ex);
        } catch (Throwable t) {
            error(message, action, logger, null).accept(t);
        }
    }

    default Consumer<Throwable> error(Message<Object> message, EventAction action, Logger logger, String overrideMsg) {
        return throwable -> {
            if (throwable instanceof DesiredException) {
                logger.debug("Failed when handle event {}", throwable, action);
            } else {
                logger.error("Failed when handle event {}", throwable, action);
            }
            Throwable t = throwable;
            if (Strings.isNotBlank(overrideMsg)) {
                t = NubeExceptionConverter.friendly(throwable, overrideMsg);
            }
            message.reply(EventMessage.error(action, t).toJson());
        };
    }

}
