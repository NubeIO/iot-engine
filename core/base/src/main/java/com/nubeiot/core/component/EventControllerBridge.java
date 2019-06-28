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

    EventController getEventController(@NonNull io.vertx.core.Vertx vertx, DeliveryOptions deliveryOptions) {
        return new DefaultEventController(vertx, deliveryOptions);
    }

    EventController getEventController(@NonNull io.vertx.core.Vertx vertx) {
        return getEventController(vertx, null);
    }

}
