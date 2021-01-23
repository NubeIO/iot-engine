package com.nubeiot.core.component;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;

import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.transport.ProxyService;

import lombok.NonNull;

public interface EventClientProxy extends ProxyService<EventbusClient> {

    static EventClientProxy create(@NonNull Vertx vertx, DeliveryOptions options) {
        return () -> new DefaultEventClient(vertx, options);
    }

    EventbusClient transporter();

}
