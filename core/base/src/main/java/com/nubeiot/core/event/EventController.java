package com.nubeiot.core.event;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.Shareable;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.transport.Transporter;

import lombok.NonNull;

//TODO: Rename to EventbusClient
//TODO: Rename `response` -> `publish`
public interface EventController extends Shareable, Transporter {

    Logger LOGGER = LoggerFactory.getLogger(EventController.class);

    /**
     * Fire the request to event address.
     * <p>
     * It is equivalent to call {@link #request(DeliveryEvent, Handler)} with no {@code reply handler}
     *
     * @param deliveryEvent Delivery Event
     */
    default void request(@NonNull DeliveryEvent deliveryEvent) {
        request(deliveryEvent, null);
    }

    /**
     * Fire the request to event address.
     * <p>
     * It is equivalent to call {@link #request(DeliveryEvent, DeliveryOptions, Handler)} with {@code deliveryOptions}
     * is {@code null}
     *
     * @param deliveryEvent Delivery Event
     * @param replyConsumer The consumer for handling message back after the system completes request process
     */
    default void request(@NonNull DeliveryEvent deliveryEvent, Handler<AsyncResult<Message<Object>>> replyConsumer) {
        request(deliveryEvent, null, replyConsumer);
    }

    /**
     * Fire the request to event address.
     * <p>
     *
     * @param deliveryEvent   Delivery Event
     * @param deliveryOptions Delivery Options
     * @param replyConsumer   The consumer for handling message back after the system completes request process
     */
    default void request(@NonNull DeliveryEvent deliveryEvent, DeliveryOptions deliveryOptions,
                         Handler<AsyncResult<Message<Object>>> replyConsumer) {
        request(deliveryEvent.getAddress(), deliveryEvent.getPattern(), deliveryEvent.payload(), replyConsumer,
                deliveryOptions);
    }

    /**
     * Fire the request to event address.
     * <p>
     * It is equivalent to call {@link #request(String, EventPattern, EventMessage, DeliveryOptions)} with {@code
     * deliveryOptions} is {@code null}
     *
     * @param address Eventbus address
     * @param pattern Event pattern
     * @param message Request data message
     */
    default void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message) {
        request(address, pattern, message, null, null);
    }

    /**
     * Fire the request to event address.
     * <p>
     * It is equivalent to call {@link #request(String, EventPattern, EventMessage, Handler, DeliveryOptions)} with
     * {@code handler} is {@code null}
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Request data message
     * @param deliveryOptions Delivery options
     * @see EventPattern
     * @see EventMessage
     * @see #request(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     */
    default void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                         DeliveryOptions deliveryOptions) {
        request(address, pattern, message, null, deliveryOptions);
    }

    /**
     * Fire the request to event address.
     * <p>
     * It is equivalent to call {@link #request(String, EventPattern, EventMessage, Handler, DeliveryOptions)} with
     * {@code deliveryOptions} is {@code null}
     *
     * @param address       Eventbus address
     * @param pattern       Event pattern
     * @param message       Request data message
     * @param replyConsumer The consumer for handling message back after the system completes request process
     * @see EventPattern
     * @see EventMessage
     * @see #request(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     */
    default void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                         Handler<AsyncResult<Message<Object>>> replyConsumer) {
        request(address, pattern, message, replyConsumer, null);
    }

    /**
     * Fire the request to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Request message message
     * @param replyConsumer   The consumer for handling message back after the system completes request process
     * @param deliveryOptions Delivery options
     * @see EventPattern
     * @see EventMessage
     */
    default void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                         Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        LOGGER.debug("Eventbus::Request:Address: {} - Pattern: {}", address, pattern);
        fire(address, pattern, message.toJson(), replyConsumer, deliveryOptions);
    }

    /**
     * Fire the response to event address.
     * <p>
     * It is equivalent to call {@link #response(String, EventPattern, EventMessage, DeliveryOptions)} with {@code
     * deliveryOptions} is {@code null}
     *
     * @param address Eventbus address
     * @param pattern Event pattern
     * @param message Event message
     * @see EventMessage
     * @see EventPattern
     */
    default void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message) {
        response(address, pattern, message, null, null);
    }

    /**
     * Fire the response to event address.
     * <p>
     * It is equivalent to call {@link #response(String, EventPattern, EventMessage, Handler, DeliveryOptions)} with
     * {@code handler} is {@code null}
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Event message
     * @param deliveryOptions Delivery options
     * @see EventMessage
     * @see EventPattern
     */
    default void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
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
     * @param deliveryOptions Delivery options
     * @see EventMessage
     * @see EventPattern
     */
    default void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
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
     * @param deliveryOptions Delivery options
     * @see EventPattern
     */
    default void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
                          DeliveryOptions deliveryOptions) {
        response(address, pattern, data, null, deliveryOptions);
    }

    /**
     * Fire the error response to event address
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param error           Error message
     * @param deliveryOptions Delivery options
     * @see EventPattern
     * @see ErrorMessage
     */
    default void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull ErrorMessage error,
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
     * @param deliveryOptions Delivery options
     * @see EventPattern
     * @see ErrorMessage
     */
    default void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull ErrorMessage error,
                          Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        LOGGER.debug("Eventbus::Error Response:Address: {} - Pattern: {}", address, pattern);
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
     * @param deliveryOptions Delivery options
     * @see EventPattern
     */
    default void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
                          Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        LOGGER.debug("Eventbus::Response:Address: {} - Pattern: {}", address, pattern);
        fire(address, pattern, data, replyConsumer, deliveryOptions);
    }

    /**
     * Fire event data to event address.
     * <p>
     * It will call response if {@code event message action} equals {@link EventAction#RETURN}, else otherwise
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Event message
     * @param deliveryOptions Delivery options
     * @see #fire(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     */
    default void fire(String address, EventPattern pattern, EventMessage message, DeliveryOptions deliveryOptions) {
        fire(address, pattern, message, null, deliveryOptions);
    }

    /**
     * Fire event data to event address.
     * <p>
     * It will call response if {@code event message action} equals {@link EventAction#RETURN}, else otherwise
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param message         Event message
     * @param replyConsumer   The consumer for handling message back
     * @param deliveryOptions Delivery options
     * @see #request(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     * @see #response(String, EventPattern, EventMessage, Handler, DeliveryOptions)
     */
    default void fire(String address, EventPattern pattern, EventMessage message,
                      Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        if (message.getAction() == EventAction.RETURN) {
            response(address, pattern, message, replyConsumer, deliveryOptions);
        } else {
            request(address, pattern, message, replyConsumer, deliveryOptions);
        }
    }

    /**
     * Fire event data to event address.
     *
     * @param address         Eventbus address
     * @param pattern         Event pattern
     * @param data            Data message
     * @param replyConsumer   The consumer for handling message back
     * @param deliveryOptions Delivery options
     */
    void fire(String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
              Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions);

    /**
     * Register event listener with event model.
     *
     * @param eventModel Event model
     * @param handler    Handler when receiving message
     * @return a reference to this, so the API can be used fluently
     * @see EventModel
     */
    default EventController register(@NonNull EventModel eventModel, @NonNull EventListener handler) {
        return this.register(eventModel.getAddress(), eventModel.isLocal(), handler);
    }

    /**
     * Register event listener.
     * <p>
     * It is equivalent to call {@link #register(String, boolean, EventListener)} with {@code local} is {@code true}
     *
     * @param address Event bus address
     * @param handler Handler when receiving message
     * @return a reference to this, so the API can be used fluently
     * @see EventListener
     */
    default EventController register(String address, @NonNull EventListener handler) {
        return this.register(address, true, handler);
    }

    /**
     * Register event listener.
     *
     * @param address Event bus address
     * @param local   If {@code true}, only register for local event address
     * @param handler Message handler when receive
     * @return a reference to this, so the API can be used fluently
     * @see EventListener
     * @see #register(String, EventListener)
     */
    EventController register(String address, boolean local, @NonNull EventListener handler);

}
