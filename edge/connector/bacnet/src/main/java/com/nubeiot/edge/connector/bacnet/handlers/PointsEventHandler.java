package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.RequestData;
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

    @EventContractor(action = EventAction.GET_LIST, returnType = EventMessage.class)
    public Single<EventMessage> getRemoteDevicePoints(RequestData data) {
        int instanceNumber = data.body().getInteger("deviceID");
        return bacnetInstance.getRemoteDeviceObjectList(instanceNumber)
                             .flatMap(item -> Single.just(EventMessage.success(EventAction.RETURN, item)));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = EventMessage.class)
    public Single<EventMessage> getRemoteDevicePointExtended(RequestData data) {
        int instanceNumber = data.body().getInteger("deviceID");
        String objectID = data.body().getString("objectID");
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
    public Single<EventMessage> writeRemoteDevicePointValue(RequestData data) {
        JsonObject body = data.body();
        int instanceNumber = body.getInteger("deviceID");
        String objectID = body.getString("objectID");
        int priority = body.getInteger("priority");
        String str = body.getString("value");
        Encodable val;

        if (priority < 1 || priority > 16) {
            return Single.error(new BACnetException("Invalid priority array index"));
        }

        if (str.equalsIgnoreCase("null")) {
            val = Null.instance;
        } else {
            try {
                val = new Real(Float.parseFloat(str));
            } catch (NumberFormatException e) {
                return Single.error(e);
            }
        }

        return bacnetInstance.writeAtPriority(instanceNumber, objectID, val, priority)
                             .flatMap(json -> Single.just(EventMessage.success(EventAction.RETURN, json)));
    }

}
