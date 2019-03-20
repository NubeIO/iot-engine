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

import lombok.Getter;

/*
 * VERTX event bus message handler
 *  calls respective messages in BACnet
 */


public class PointsEventHandler implements EventHandler {

    private BACnet bacnetInstance;
    private final Vertx vertx;
    @Getter
    private final List<EventAction> availableEvents;

    public PointsEventHandler(Vertx vertx, BACnet bacnetInstance, EventModel eventModel) {
        this.vertx = vertx;
        this.bacnetInstance = bacnetInstance;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = EventMessage.class)
    public Single<EventMessage> getRemoteDevicePoints(Map<String, Object> message) {
        int instanceNumber = JsonObject.mapFrom(message).getInteger("deviceID");
        return bacnetInstance.getRemoteDeviceObjectList(instanceNumber)
                             .flatMap(item -> Single.just(EventMessage.success(EventAction.RETURN, item)));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = EventMessage.class)
    public Single<EventMessage> getRemoteDevicePointExtended(Map<String, Object> message) {
        JsonObject data = JsonObject.mapFrom(message);
        int instanceNumber = data.getInteger("deviceID");
        String objectID = data.getString("objectID");
        return bacnetInstance.getRemoteObjectProperties(instanceNumber, objectID)
                             .flatMap(item -> Single.just(EventMessage.success(EventAction.RETURN, item)));
    }
    //
    //    @EventContractor(action = EventAction.CREATE, returnType = EventMessage.class)
    //    public Single<EventMessage> saveRemoteDevicePoint(Map<String, Object> message) {
    //
    //    }
    //
    //    @EventContractor(action = EventAction.REMOVE, returnType = EventMessage.class)
    //    public Single<EventMessage> removeRemoteDevicePoint(Map<String, Object> message) {
    //
    //    }
    //
    //    @EventContractor(action = EventAction.PATCH, returnType = EventMessage.class)
    //    public Single<EventMessage> writeRemoteDevicePoint(Map<String, Object> message) {
    //
    //    }

}
