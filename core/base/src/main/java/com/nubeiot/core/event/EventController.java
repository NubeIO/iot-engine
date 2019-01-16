package com.nubeiot.core.event;

import java.util.Objects;
import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.Shareable;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Represents {@code Eventbus} controller to {@code send}, {@code publish}, {@code register} event
 *
 * @see EventMessage
 * @see ErrorMessage
 */
// TODO: Add event bus options/delivery options.
public final class EventController implements Shareable {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    //    private final Vertx vertx;
    private final EventBus eventBus;

    public EventController(@NonNull io.vertx.core.Vertx vertx) {
        //        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
    }

    public EventController(@NonNull Vertx vertx) {
        this(vertx.getDelegate());
    }

    /**
     * Fire the request to event address
     *
     * @param address Eventbus address
     * @param pattern Event pattern
     * @param message Request data message
     * @see EventPattern
     * @see EventMessage
     * @see #request(String, EventPattern, EventMessage, Consumer)
     */
    public void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message) {
        request(address, pattern, message, null);
    }

    /**
     * Fire the request to event address
     *
     * @param address       Eventbus address
     * @param pattern       Event pattern
     * @param message       Request message message
     * @param replyConsumer The consumer for handling message back after the system completes request process
     * @see EventPattern
     * @see EventMessage
     */
    public void request(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                        Consumer<AsyncResult<Message<Object>>> replyConsumer) {
        logger.debug("Eventbus::Request:Address: {} - Pattern: {}", address, pattern);
        fire(address, pattern, message.toJson(), replyConsumer);
    }

    /**
     * Fire the response to event address
     *
     * @param address Eventbus address
     * @param pattern Event pattern
     * @param message Event message
     * @see EventMessage
     * @see EventPattern
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message) {
        response(address, pattern, message, null);
    }

    /**
     * Fire the response to event address
     *
     * @param address       Eventbus address
     * @param pattern       Event pattern
     * @param message       Event message
     * @param replyConsumer The consumer for handling message back after an external system completes handling response
     * @see EventMessage
     * @see EventPattern
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull EventMessage message,
                         Consumer<AsyncResult<Message<Object>>> replyConsumer) {
        if (message.isError()) {
            response(address, pattern, message.getError(), replyConsumer);
        } else {
            response(address, pattern, message.toJson(), replyConsumer);
        }
    }

    /**
     * Fire the response to event address
     *
     * @param address Eventbus address
     * @param pattern Event pattern
     * @param data    Response data
     * @see EventPattern
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull JsonObject data) {
        response(address, pattern, data, null);
    }

    /**
     * Fire the error response to event address
     *
     * @param address Eventbus address
     * @param pattern Event pattern
     * @param error   Error message
     * @see EventPattern
     * @see ErrorMessage
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull ErrorMessage error) {
        response(address, pattern, error.toJson(), null);
    }

    /**
     * Fire the error response to event address
     *
     * @param address       Eventbus address
     * @param pattern       Event pattern
     * @param error         Error message
     * @param replyConsumer The consumer for handling message back after an external system completes handling response
     * @see EventPattern
     * @see ErrorMessage
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull ErrorMessage error,
                         Consumer<AsyncResult<Message<Object>>> replyConsumer) {
        logger.debug("Eventbus::Error Response:Address: {} - Pattern: {}", address, pattern);
        fire(address, pattern, error.toJson(), replyConsumer);
    }

    /**
     * Fire the response to event address
     *
     * @param address       Eventbus address
     * @param pattern       Event pattern
     * @param data          Response data
     * @param replyConsumer The consumer for handling message back after an external system completes handling *
     *                      response
     * @see EventPattern
     */
    public void response(@NonNull String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
                         Consumer<AsyncResult<Message<Object>>> replyConsumer) {
        logger.debug("Eventbus::Response:Address: {} - Pattern: {}", address, pattern);
        fire(address, pattern, data, replyConsumer);
    }

    /**
     * Fire event data to event address
     * <p>
     * It will call response if {@code event message action} equals {@link EventAction#RETURN}, else otherwise
     *
     * @param address Eventbus address
     * @param pattern Event pattern
     * @param message Event message
     * @see #fire(String, EventPattern, EventMessage, Consumer)
     */
    public void fire(String address, EventPattern pattern, EventMessage message) {
        fire(address, pattern, message, null);
    }

    /**
     * Fire event data to event address
     * <p>
     * It will call response if {@code event message action} equals {@link EventAction#RETURN}, else otherwise
     *
     * @param address       Eventbus address
     * @param pattern       Event pattern
     * @param message       Event message
     * @param replyConsumer The consumer for handling message back
     * @see #request(String, EventPattern, EventMessage, Consumer)
     * @see #response(String, EventPattern, EventMessage, Consumer)
     */
    public void fire(String address, EventPattern pattern, EventMessage message,
                     Consumer<AsyncResult<Message<Object>>> replyConsumer) {
        if (message.getAction() == EventAction.RETURN) {
            response(address, pattern, message, replyConsumer);
        } else {
            request(address, pattern, message, replyConsumer);
        }
    }

    /**
     * Register event consumer
     *
     * @param address Event bus address
     * @param handler Handler when receiving message
     * @see EventHandler
     */
    public void register(String address, @NonNull EventHandler handler) {
        this.register(address, true, handler);
    }

    /**
     * Register event consumer
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
            eventBus.localConsumer(address, handler::accept);
        } else {
            eventBus.consumer(address, handler::accept);
        }
    }

    /**
     * Register event consumer
     *
     * @param eventModel Event model
     * @param handler    Handler when receiving message
     * @see EventModel
     */
    public void register(@NonNull EventModel eventModel, @NonNull EventHandler handler) {
        this.register(eventModel.getAddress(), eventModel.isLocal(), handler);
    }

    private void fire(String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
                      Consumer<AsyncResult<Message<Object>>> replyConsumer) {
        Strings.requireNotBlank(address);
        if (pattern == EventPattern.PUBLISH_SUBSCRIBE) {
            eventBus.publish(address, data);
        }
        if (pattern == EventPattern.POINT_2_POINT) {
            eventBus.send(address, data);
        }
        if (pattern == EventPattern.REQUEST_RESPONSE) {
            Objects.requireNonNull(replyConsumer, "Must provide reply consumer");
            eventBus.send(address, data, replyConsumer::accept);
        }
    }

}
