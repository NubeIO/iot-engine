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
import com.serotonin.bacnet4j.type.Encodable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/*
 * VERTX event bus message handler
 *  calls respective messages in BACnetInstance
 */
@RequiredArgsConstructor
public class RemotePointEventHandler implements EventHandler {

    private final Map<String, BACnetInstance> bacnetInstances;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.POINT.getEvents()));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> readRemoteDevicePointValue(@Param("network") String network,
                                                         @Param("deviceId") int instanceNumber,
                                                         @Param("objectId") String objectId) {

        try {
            return bacnetInstances.get(network).readRemoteObjectvalue(instanceNumber, objectId);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> writeRemoteDevicePointValue(@Param("network") String network,
                                                          @Param("deviceId") int instanceNumber,
                                                          @Param("objectId") String objectId,
                                                          @Param("priority") int priority, @Param("value") Object obj) {

        try {
            return bacnetInstances.get(network).writeAtPriority(instanceNumber, objectId, obj, priority);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }
}
