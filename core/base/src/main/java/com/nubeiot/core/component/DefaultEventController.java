package com.nubeiot.core.component;

import java.util.Objects;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventMessage;
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
final class DefaultEventController implements EventController {

    private final EventBus eventBus;
    private final DeliveryOptions deliveryOptions;

    DefaultEventController(@NonNull Vertx vertx) {
        this(vertx, null);
    }

    DefaultEventController(@NonNull Vertx vertx, DeliveryOptions deliveryOptions) {
        this(vertx.eventBus(), deliveryOptions);
    }

    private DefaultEventController(@NonNull EventBus eventBus, DeliveryOptions deliveryOptions) {
        this.eventBus = eventBus;
        this.deliveryOptions = Objects.nonNull(deliveryOptions) ? deliveryOptions : new DeliveryOptions();
    }

    /**
     * {@inheritDoc}
     */
    public void fire(String address, @NonNull EventPattern pattern, @NonNull JsonObject data,
                     Handler<AsyncResult<Message<Object>>> replyConsumer, DeliveryOptions deliveryOptions) {
        DeliveryOptions options = Objects.nonNull(deliveryOptions) ? deliveryOptions : this.deliveryOptions;
        Strings.requireNotBlank(address);
        if (pattern == EventPattern.PUBLISH_SUBSCRIBE) {
            eventBus.publish(address, data, options);
        }
        if (pattern == EventPattern.POINT_2_POINT) {
            eventBus.send(address, data, options);
        }
        if (pattern == EventPattern.REQUEST_RESPONSE) {
            Objects.requireNonNull(replyConsumer, "Must provide reply consumer");
            eventBus.send(address, data, options, replyConsumer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void register(String address, boolean local, @NonNull EventListener handler) {
        Strings.requireNotBlank(address);
        if (local) {
            this.eventBus.localConsumer(address, handler::accept);
        } else {
            this.eventBus.consumer(address, handler::accept);
        }
    }

    @Override
    public Shareable copy() {
        return new DefaultEventController(eventBus, new DeliveryOptions(deliveryOptions.toJson()));
    }

}
