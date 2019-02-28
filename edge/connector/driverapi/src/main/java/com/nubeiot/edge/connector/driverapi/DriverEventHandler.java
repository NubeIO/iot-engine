package com.nubeiot.edge.connector.driverapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;

import lombok.Getter;
import lombok.NonNull;

public class DriverEventHandler implements EventHandler {

    private final Vertx vertx;
    @Getter
    private final List<EventAction> availableEvents;

    public DriverEventHandler(@NonNull Vertx vertx, @NonNull EventModel eventModel) {
        this.vertx = vertx;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = JsonObject.class)
    public JsonObject getList(RequestData data) {
        return new JsonObject("{\"test\":\"test test\"}");
    }

}
