package com.nubeiot.core.event;

import java.util.Objects;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;

public abstract class EventController {

    @Getter
    private final EventBus eventBus;
    private final DeliveryOptions deliveryOptions;

    public EventController(@NonNull io.vertx.core.Vertx vertx, DeliveryOptions deliveryOptions) {
        this.eventBus = vertx.eventBus();
        this.deliveryOptions = Objects.nonNull(deliveryOptions) ? deliveryOptions : new DeliveryOptions();
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
    public abstract void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
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
    public abstract void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
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
    public abstract void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
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
    public abstract void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
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
    public abstract void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
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
    public abstract void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull ErrorMessage error,
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
    public abstract void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull ErrorMessage error,
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
    public abstract void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
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
    public abstract void fire(String address, EventPattern pattern, EventMessage message,
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
    public abstract void fire(String address, EventPattern pattern, EventMessage message,
                              Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions);

    /**
     * Register event listener
     *
     * @param address Event bus address
     * @param handler Handler when receiving message
     * @see EventHandler
     */
    public abstract void register(String address, @NonNull EventHandler handler);

    /**
     * Register event listener
     *
     * @param address Event bus address
     * @param local   If {@code true}, only register for local event address
     * @param handler Message handler when receive
     * @see EventHandler
     * @see #register(String, EventHandler)
     */
    public abstract void register(String address, boolean local, @NonNull EventHandler handler);

    /**
     * Register event listener
     *
     * @param eventModel Event model
     * @param handler    Handler when receiving message
     * @see EventModel
     */
    public abstract void register(@NonNull EventModel eventModel, @NonNull EventHandler handler);

    protected void fire(String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
                        Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        DeliveryOptions registerOptions = Objects.nonNull(deliveryOptions) ? deliveryOptions : this.deliveryOptions;
        Strings.requireNotBlank(address);
        if (pattern == EventPattern.PUBLISH_SUBSCRIBE) {
            eventBus.publish(address, data, registerOptions);
        }
        if (pattern == EventPattern.POINT_2_POINT) {
            if (deliveryOptions == null) {
                eventBus.send(address, data);
            } else {
                eventBus.send(address, data, deliveryOptions);
            }
        }
        if (pattern == EventPattern.REQUEST_RESPONSE) {
            Objects.requireNonNull(replyConsumer, "Must provide reply consumer");
            if (deliveryOptions == null) {
                eventBus.send(address, data, replyConsumer);
            } else {
                eventBus.send(address, data, deliveryOptions, replyConsumer);
            }
        }
        if (pattern == EventPattern.REQUEST_RESPONSE) {
            Objects.requireNonNull(replyConsumer, "Must provide reply consumer");
            eventBus.send(address, data, registerOptions, replyConsumer);
        }
    }

}
