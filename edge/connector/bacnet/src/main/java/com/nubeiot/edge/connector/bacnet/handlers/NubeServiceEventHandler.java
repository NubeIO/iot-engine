package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.edge.connector.bacnet.BACnetEventModels;
import com.nubeiot.edge.connector.bacnet.BACnetInstance;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/*
 * VERTX event bus message handler
 *  calls respective messages in BACnetInstance
 */
@RequiredArgsConstructor
public class NubeServiceEventHandler implements EventHandler {

    private final Map<String, BACnetInstance> bacnetInstances;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.NUBE_SERVICE_SUB.getEvents()));
    }

    @EventContractor(action = EventAction.CREATE, returnType = void.class)
    public void objectCreated(@Param("id") String id, @Param("data") JsonObject data) {
        bacnetInstances.forEach((s, baCnetInstance) -> baCnetInstance.addLocalObjectFromJson(id, data));
    }

    @EventContractor(action = EventAction.REMOVE, returnType = void.class)
    public void objectRemoved(@Param("id") String id) {
        bacnetInstances.forEach((s, baCnetInstance) -> baCnetInstance.removeLocalObject(id));
    }

    @EventContractor(action = EventAction.PATCH, returnType = void.class)
    public void objectWritten(@Param("id") String id, @Param("data") JsonObject data) {
        bacnetInstances.forEach((s, baCnetInstance) -> baCnetInstance.writeLocalObject(id, data));
    }

    @EventContractor(action = EventAction.PATCH, returnType = void.class)
    public void objectUpdated(@Param("id") String id, @Param("property") String property,
                              @Param("value") Object value) {
        bacnetInstances.forEach((s, baCnetInstance) -> baCnetInstance.updateLocalObject(id, property, value));
    }

    @EventContractor(action = EventAction.UPDATE, returnType = void.class)
    public void updateAll(@Param("points") JsonObject points) {
        bacnetInstances.forEach((s, baCnetInstance) -> baCnetInstance.initialiseLocalObjectsFromJson(points));
    }

}
