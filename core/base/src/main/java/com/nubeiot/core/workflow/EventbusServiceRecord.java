package com.nubeiot.core.workflow;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.DeliveryEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EventbusServiceRecord implements ServiceRecord {

    private final JsonObject location;
    private final JsonObject metadata;

    static EventbusServiceRecord create(DeliveryEvent event) {
        return new EventbusServiceRecord(new JsonObject().put("address", event.getAddress()), event.toJson());
    }

    @Override
    public String type() {
        return "eventbus";
    }

    @Override
    public JsonObject location() {
        return location;
    }

    @Override
    public JsonObject metadata() {
        return metadata;
    }

}
