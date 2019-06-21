package com.nubeiot.edge.module.gateway.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.module.gateway.EdgeGatewayVerticle;

import lombok.Getter;
import lombok.NonNull;

public class DriverEventHandler implements EventHandler {

    @Getter
    private final List<EventAction> availableEvents;
    private final EdgeGatewayVerticle verticle;

    public DriverEventHandler(EdgeGatewayVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        return verticle.getMicroContext()
                       .getLocalController()
                       .getRecords()
                       .flatMap(records -> Observable.fromIterable(records).map(Record::toJson).toList())
                       .map(records -> new JsonObject().put("records", new JsonArray(records)));
    }

}
