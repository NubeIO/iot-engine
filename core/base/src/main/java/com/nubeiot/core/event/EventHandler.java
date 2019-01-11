package com.nubeiot.core.event;

import java.util.List;
import java.util.function.Consumer;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public interface EventHandler extends Consumer<Message<Object>> {

    /**
     * Available events that this handler can process
     *
     * @return list of possible events
     */
    @NonNull List<EventAction> getAvailableEvents();

    @SuppressWarnings("unchecked")
    default void accept(io.vertx.reactivex.core.eventbus.Message<Object> message) {
        this.accept(message.getDelegate());
    }

    /**
     * Jackson Object mapper for serialize/deserialize data
     *
     * @return Object mapper. Default: {@link Json#mapper}
     */
    default ObjectMapper mapper() { return Json.mapper; }

    /**
     * Fallback json key if output is {@code collection/primitive}  value
     *
     * @return fallback json key. Default: {@code data}
     */
    default String fallback() { return "data"; }

    @Override
    default void accept(Message<Object> message) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        EventMessage msg = EventMessage.from(message.body());
        EventAction action = msg.getAction();
        logger.info("Executing action: {} with data: {}", action, msg.toJson().encode());
        AnnotationHandler<? extends EventHandler> handler = new AnnotationHandler<>(this);
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
            logger.error("Failed when handle event {}", throwable, action);
            Throwable t = throwable;
            if (Strings.isNotBlank(overrideMsg)) {
                t = NubeExceptionConverter.friendly(throwable, overrideMsg);
            }
            message.reply(EventMessage.error(action, t).toJson());
        };
    }

}
