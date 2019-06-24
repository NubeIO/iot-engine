package com.nubeiot.core.component;

import java.util.Objects;

import io.vertx.core.eventbus.DeliveryOptions;

import com.nubeiot.core.event.EventController;

import lombok.NonNull;

public class EventControllerBridge {

    private static EventControllerBridge instance;

    public static EventControllerBridge getInstance() {
        if (Objects.nonNull(instance)) {
            return instance;
        }

        instance = new EventControllerBridge();
        return instance;
    }

    public EventController getEventController(@NonNull io.vertx.core.Vertx vertx, DeliveryOptions deliveryOptions) {
        return new DefaultEventController(vertx, deliveryOptions);
    }

    public EventController getEventController(@NonNull io.vertx.reactivex.core.Vertx vertx) {
        return getEventController(vertx.getDelegate(), null);
    }

    public EventController getEventController(@NonNull io.vertx.core.Vertx vertx) {
        return getEventController(vertx, null);
    }

    public EventController getEventController(@NonNull io.vertx.reactivex.core.Vertx vertx,
                                              DeliveryOptions deliveryOptions) {
        return getEventController(vertx.getDelegate(), deliveryOptions);
    }

}
