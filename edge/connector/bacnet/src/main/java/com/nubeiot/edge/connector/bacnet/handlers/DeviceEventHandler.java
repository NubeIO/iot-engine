package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.connector.bacnet.BACnet;
import com.nubeiot.edge.connector.bacnet.BACnetEventModels;

import lombok.Getter;

/*
 * VERTX event bus message handler
 *  calls respective messages in BACnet
 */


public class DeviceEventHandler implements EventHandler {

    private BACnet bacnetInstance;
    private final Vertx vertx;
    @Getter
    private final List<EventAction> availableEvents;

    public DeviceEventHandler(Vertx vertx, BACnet bacnetInstance, EventModel eventModel) {
        this.vertx = vertx;
        this.bacnetInstance = bacnetInstance;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.POINTS.getEvents()));
    }

    //GET ALL DEVICES
    @EventContractor(action = EventAction.GET_LIST, returnType = JsonObject.class)
    public Single<JsonObject> getList(Map<String, Object> message) {
        return bacnetInstance.getRemoteDevices();
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = EventMessage.class)
    public Single<EventMessage> getRemoteDeviceExtendedInfo(Map<String, Object> message) {
        JsonObject data = JsonObject.mapFrom(message);
        int instanceNumber = data.getInteger("deviceID");
        return bacnetInstance.getRemoteDeviceExtendedInfo(instanceNumber)
                             .flatMap(jsonObject -> Single.just(EventMessage.success(EventAction.RETURN, jsonObject)));
    }

}
