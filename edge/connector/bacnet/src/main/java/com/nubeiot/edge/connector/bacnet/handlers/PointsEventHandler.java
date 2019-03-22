package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.edge.connector.bacnet.BACnet;
import com.nubeiot.edge.connector.bacnet.BACnetEventModels;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.Real;

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

    public PointsEventHandler(Vertx vertx, BACnet bacnetInstance) {
        this.vertx = vertx;
        this.bacnetInstance = bacnetInstance;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.POINTS.getEvents()));
    }

    //TODO: should these be eventActions or HttpMethods since they're only ever going to be accessed over the http
    // rest api
    @EventContractor(action = EventAction.GET_LIST, returnType = EventMessage.class)
    //    public Single<EventMessage> getRemoteDevicePoints(Map<String, Object> message) {
    public Single<EventMessage> getRemoteDevicePoints(JsonObject message) {
        int instanceNumber = JsonObject.mapFrom(message).getInteger("deviceID");
        return bacnetInstance.getRemoteDeviceObjectList(instanceNumber)
                             .flatMap(item -> Single.just(EventMessage.success(EventAction.RETURN, item)));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = EventMessage.class)
    //    public Single<EventMessage> getRemoteDevicePointExtended(Map<String, Object> message) {
    public Single<EventMessage> getRemoteDevicePointExtended(JsonObject message) {
        JsonObject data = JsonObject.mapFrom(message);
        int instanceNumber = data.getInteger("deviceID");
        String objectID = data.getString("objectID");
        return bacnetInstance.getRemoteObjectProperties(instanceNumber, objectID)
                             .flatMap(item -> Single.just(EventMessage.success(EventAction.RETURN, item)));
    }

    //    @EventContractor(action = EventAction.CREATE, returnType = EventMessage.class)
    //    public Single<EventMessage> saveRemoteDevicePoint(Map<String, Object> message) {
    //
    //    }
    //
    //    @EventContractor(action = EventAction.REMOVE, returnType = EventMessage.class)
    //    public Single<EventMessage> removeRemoteDevicePoint(Map<String, Object> message) {
    //
    //    }

    @EventContractor(action = EventAction.PATCH, returnType = EventMessage.class)
    //    public Single<EventMessage> writeRemoteDevicePointValue(Map<String, Object> message) {
    public Single<EventMessage> writeRemoteDevicePointValue(JsonObject message) {
        JsonObject data = JsonObject.mapFrom(message);
        int instanceNumber = data.getInteger("deviceID");
        String objectID = data.getString("objectID");
        int priority = data.getInteger("priority");
        String str = data.getString("value");
        Encodable val;

        if (priority < 1 || priority > 16) {
            return Single.error(new BACnetException("Invalid priority array index"));
        }

        if (str.equalsIgnoreCase("null")) {
            val = Null.instance;
        } else {
            try {
                val = new Real(Float.parseFloat(str));
            } catch (Exception e) {
                return Single.error(e);
            }
        }

        return bacnetInstance.writeAtPriority(instanceNumber, objectID, val, priority)
                             .flatMap(json -> Single.just(EventMessage.success(EventAction.RETURN, json)));
    }




}
