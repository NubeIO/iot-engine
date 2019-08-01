package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventHandler;
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
public class RemotePointsInfoEventHandler implements EventHandler {

    private final Map<String, BACnetInstance> bacnetInstances;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.POINTS_INFO.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getRemoteDevicePoints(@Param("network") String network,
                                                    @Param("deviceId") String instanceNumber) {
        try {
            return bacnetInstances.get(network).getRemoteDeviceObjectList(Integer.parseInt(instanceNumber));
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        } catch (ClassCastException e) {
            return Single.error(e);
        }
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getRemoteDevicePointExtended(@Param("network") String network,
                                                           @Param("deviceId") String instanceNumber,
                                                           @Param("objectId") String objectId) {
        try {
            return bacnetInstances.get(network).getRemoteObjectProperties(Integer.parseInt(instanceNumber), objectId);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        } catch (ClassCastException e) {
            return Single.error(e);
        }
    }

}
