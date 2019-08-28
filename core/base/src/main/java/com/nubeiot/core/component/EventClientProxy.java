package com.nubeiot.core.component;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.transport.ProxyService;
import com.nubeiot.core.transport.Transporter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventClientProxy implements ProxyService {

    private EventController client;

    static EventClientProxy create(@NonNull Vertx vertx, JsonObject config) {
        return new EventClientProxy().init(vertx, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public EventClientProxy init(@NonNull Vertx vertx, JsonObject config) {
        this.client = new DefaultEventClient(vertx, config);
        return this;
    }

    @Override
    public Transporter transporter() {
        return client;
    }

}
