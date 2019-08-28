package com.nubeiot.core.component;

import java.util.Objects;
import java.util.Optional;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;

/**
 * Represents {@code Eventbus} controller to {@code send}, {@code publish}, {@code register} event
 *
 * @see EventMessage
 * @see ErrorMessage
 */
final class DefaultEventClient implements EventController {

    @Getter
    private final Vertx vertx;
    private final DeliveryOptions deliveryOptions;

    DefaultEventClient(@NonNull Vertx vertx) {
        this(vertx, (DeliveryOptions) null);
    }

    DefaultEventClient(@NonNull Vertx vertx, JsonObject config) {
        this(vertx, Optional.ofNullable(config).map(DeliveryOptions::new).orElse(null));
    }

    DefaultEventClient(@NonNull Vertx vertx, DeliveryOptions deliveryOptions) {
        this.vertx = vertx;
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
            vertx.eventBus().publish(address, data, options);
        }
        if (pattern == EventPattern.POINT_2_POINT) {
            vertx.eventBus().send(address, data, options);
        }
        if (pattern == EventPattern.REQUEST_RESPONSE) {
            Objects.requireNonNull(replyConsumer, "Must provide reply consumer");
            vertx.eventBus().send(address, data, options, replyConsumer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public EventController register(String address, boolean local, @NonNull EventListener handler) {
        LOGGER.info("Registering {} Event Listener '{}' | Address '{}'...", local ? "Local" : "Cluster",
                    handler.getClass().getName(), Strings.requireNotBlank(address));
        if (local) {
            vertx.eventBus().localConsumer(address, handler::accept);
        } else {
            vertx.eventBus().consumer(address, handler::accept);
        }
        return this;
    }

    @Override
    public Shareable copy() {
        return new DefaultEventClient(vertx, new DeliveryOptions(deliveryOptions.toJson()));
    }

}
