package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.connector.bacnet.BACnetEventModels;
import com.nubeiot.edge.connector.bacnet.BACnetInstance;
import com.serotonin.bacnet4j.exception.BACnetException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/*
 * VERTX event bus message handler
 *  calls respective messages in BACnetInstance
 */
@RequiredArgsConstructor
public class RemotePointsInfoEventHandler implements EventListener {

    private final Map<String, BACnetInstance> bacnetInstances;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.POINTS_INFO.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getRemoteDevicePoints(RequestData requestData) {
        String network = requestData.body().getString("network");
        Integer instanceNumber = Integer.parseInt(requestData.body().getString("deviceId"));
        try {
            return bacnetInstances.get(network).getRemoteDeviceObjectList(instanceNumber);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        } catch (ClassCastException e) {
            return Single.error(e);
        }
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getRemoteDevicePointExtended(RequestData requestData) {
        String network = requestData.body().getString("network");
        Integer instanceNumber = Integer.parseInt(requestData.body().getString("deviceId"));
        String objectId = requestData.body().getString("objectId");

        try {
            return bacnetInstances.get(network).getRemoteObjectProperties(instanceNumber, objectId);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        } catch (ClassCastException e) {
            return Single.error(e);
        }
    }

}
