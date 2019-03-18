package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventMessage;
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

    public DeviceEventHandler(Vertx vertx, BACnet bacnetInstance) {
        this.vertx = vertx;
        this.bacnetInstance = bacnetInstance;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.DEVICES.getEvents()));
    }

    //GET ALL DEVICES
    @EventContractor(action = EventAction.GET_LIST, returnType = EventMessage.class)
    public Single<EventMessage> getCachedRemoteDevices(RequestData data) {
        return bacnetInstance.getRemoteDevices()
                             .flatMap(item -> Single.just(EventMessage.success(EventAction.RETURN, item)));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = EventMessage.class)
    public Single<EventMessage> getRemoteDeviceExtendedInfo(RequestData data) {
        int instanceNumber = data.body().getInteger("deviceID");
        return bacnetInstance.getRemoteDeviceExtendedInfo(instanceNumber)
                             .flatMap(jsonObject -> Single.just(EventMessage.success(EventAction.RETURN, jsonObject)));
    }
}
