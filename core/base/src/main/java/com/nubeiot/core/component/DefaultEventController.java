package com.nubeiot.core.component;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.Shareable;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Represents {@code Eventbus} controller to {@code send}, {@code publish}, {@code register} event
 *
 * @see EventMessage
 * @see ErrorMessage
 */
final class DefaultEventController extends EventController implements Shareable {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventController.class);
    //    private final Vertx vertx;

    public DefaultEventController(@NonNull io.vertx.core.Vertx vertx, DeliveryOptions deliveryOptions) {
        super(vertx, deliveryOptions);
    }

    public DefaultEventController(@NonNull Vertx vertx, DeliveryOptions deliveryOptions) {
        this(vertx.getDelegate(), deliveryOptions);
    }

    /**
     * Fire the request to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Request data message
     * @param deliveryOptions
     * @see EventPattern
     * @see EventMessage
     * @see #request(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     */
    public void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                        DeliveryOptions deliveryOptions) {
        request(address, pattern, message, null, deliveryOptions);
    }

    /**
     * Fire the request to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Request message message
     * @param replyConsumer   The consumer for handling message back after the system completes request process
     * @param deliveryOptions
     * @see EventPattern
     * @see EventMessage
     */
    public void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                        Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        logger.debug("Eventbus::Request:Address: {} - Pattern: {}", address, pattern);
        fire(address, pattern, message.toJson(), replyConsumer, deliveryOptions);
    }

    /**
     * Fire the response to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Event message
     * @param deliveryOptions
     * @see EventMessage
     * @see EventPattern
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                         DeliveryOptions deliveryOptions) {
        response(address, pattern, message, null, deliveryOptions);
    }

    /**
     * Fire the response to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Event message
     * @param replyConsumer   The consumer for handling message back after an external system completes handling
     *                        response
     * @param deliveryOptions
     * @see EventMessage
     * @see EventPattern
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                         Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        if (message.isError()) {
            response(address, pattern, message.getError(), replyConsumer, deliveryOptions);
        } else {
            response(address, pattern, message.toJson(), replyConsumer, deliveryOptions);
        }
    }

    /**
     * Fire the response to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param data            Response data
     * @param deliveryOptions
     * @see EventPattern
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
                         DeliveryOptions deliveryOptions) {
        response(address, pattern, data, null, deliveryOptions);
    }

    /**
     * Fire the error response to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param error           Error message
     * @param deliveryOptions
     * @see EventPattern
     * @see ErrorMessage
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull ErrorMessage error,
                         DeliveryOptions deliveryOptions) {
        response(address, pattern, error.toJson(), null, deliveryOptions);
    }

    /**
     * Fire the error response to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param error           Error message
     * @param replyConsumer   The consumer for handling message back after an external system completes handling
     *                        response
     * @param deliveryOptions
     * @see EventPattern
     * @see ErrorMessage
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull ErrorMessage error,
                         Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        logger.debug("Eventbus::Error Response:Address: {} - Pattern: {}", address, pattern);
        fire(address, pattern, error.toJson(), replyConsumer, deliveryOptions);
    }

    /**
     * Fire the response to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param data            Response data
     * @param replyConsumer   The consumer for handling message back after an external system completes handling *
     *                        response
     * @param deliveryOptions
     * @see EventPattern
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
                         Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        logger.debug("Eventbus::Response:Address: {} - Pattern: {}", address, pattern);
        fire(address, pattern, data, replyConsumer, deliveryOptions);
    }

    /**
     * Fire event data to event address
     * <p>
     * It will call response if {@code event message action} equals {@link EventAction#RETURN}, else otherwise
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Event message
     * @param deliveryOptions
     * @see #fire(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     */
    public void fire(String address, EventPattern pattern, EventMessage message, DeliveryOptions deliveryOptions) {
        fire(address, pattern, message, null, deliveryOptions);
    }

    /**
     * Fire event data to event address
     * <p>
     * It will call response if {@code event message action} equals {@link EventAction#RETURN}, else otherwise
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Event message
     * @param replyConsumer   The consumer for handling message back
     * @param deliveryOptions
     * @see #request(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     * @see #response(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     */
    public void fire(String address, EventPattern pattern, EventMessage message,
                     Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        if (message.getAction() == EventAction.RETURN) {
            response(address, pattern, message, replyConsumer, deliveryOptions);
        } else {
            request(address, pattern, message, replyConsumer, deliveryOptions);
        }
    }

    /**
     * Register event listener
     *
     * @param address Event bus address
     * @param handler Handler when receiving message
     * @see EventHandler
     */
    public void register(String address, @NonNull EventHandler handler) {
        this.register(address, true, handler);
    }

    /**
     * Register event listener
     *
     * @param address Event bus address
     * @param local   If {@code true}, only register for local event address
     * @param handler Message handler when receive
     * @see EventHandler
     * @see #register(String, EventHandler)
     */
    public void register(String address, boolean local, @NonNull EventHandler handler) {
        Strings.requireNotBlank(address);
        if (local) {
            this.getEventBus().localConsumer(address, handler::accept);
        } else {
            this.getEventBus().consumer(address, handler::accept);
        }
    }

    /**
     * Register event listener
     *
     * @param eventModel Event model
     * @param handler    Handler when receiving message
     * @see EventModel
     */
    public void register(@NonNull EventModel eventModel, @NonNull EventHandler handler) {
        this.register(eventModel.getAddress(), eventModel.isLocal(), handler);
    }

}
