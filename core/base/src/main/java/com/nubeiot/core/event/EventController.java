package com.nubeiot.core.event;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.ErrorMessage;

import lombok.NonNull;

public interface EventController {

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
                        DeliveryOptions deliveryOptions);

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
                        Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions);

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
                         DeliveryOptions deliveryOptions);

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
                         Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions);

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
                         DeliveryOptions deliveryOptions);

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
                         DeliveryOptions deliveryOptions);

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
                         Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions);

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
                         Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions);

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
    public void fire(String address, EventPattern pattern, EventMessage message,
                     DeliveryOptions deliveryOptions);

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
                     Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions);

    /**
     * Register event listener
     *
     * @param address Event bus address
     * @param handler Handler when receiving message
     * @see EventHandler
     */
    public void register(String address, @NonNull EventHandler handler);

    /**
     * Register event listener
     *
     * @param address Event bus address
     * @param local   If {@code true}, only register for local event address
     * @param handler Message handler when receive
     * @see EventHandler
     * @see #register(String, EventHandler)
     */
    public void register(String address, boolean local, @NonNull EventHandler handler);

    /**
     * Register event listener
     *
     * @param eventModel Event model
     * @param handler    Handler when receiving message
     * @see EventModel
     */
    public void register(@NonNull EventModel eventModel, @NonNull EventHandler handler);

}
