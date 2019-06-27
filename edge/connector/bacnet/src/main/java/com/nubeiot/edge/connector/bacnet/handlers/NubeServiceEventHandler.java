package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.connector.bacnet.BACnetEventModels;
import com.nubeiot.edge.connector.bacnet.BACnetInstance;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgePoint;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgeWriteRequest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/*
 * VERTX event bus message handler
 *  calls respective messages in BACnetInstance
 */
@RequiredArgsConstructor
public class NubeServiceEventHandler implements EventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, BACnetInstance> bacnetInstances;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.NUBE_SERVICE.getEvents()));
    }

    @EventContractor(action = EventAction.CREATE, returnType = void.class)
    public void objectCreated(@Param("id") String id, @Param("data") JsonObject data) {
        EdgePoint point = EdgePoint.fromJson(id, data);
        bacnetInstances.forEach((s, bacnetInstance) -> {
            try {
                bacnetInstance.addLocalObject(point);
            } catch (Exception e) {
                logger.warn("Error creating point {} ", e, id);
                return;
            }
        });
    }

    @EventContractor(action = EventAction.REMOVE, returnType = void.class)
    public void objectRemoved(@Param("id") String id) {
        bacnetInstances.forEach((s, bacnetInstance) -> {
            try {
                bacnetInstance.removeLocalObject(id);
            } catch (Exception e) {
                logger.warn("Error removing point {}", e, id);
                return;
            }
        });
    }

    @EventContractor(action = EventAction.PATCH, returnType = void.class)
    public void objectWritten(@Param("id") String id, @Param("data") JsonObject data) {
        EdgeWriteRequest req = EdgeWriteRequest.fromJson(id, data);
        bacnetInstances.forEach((s, bacnetInstance) -> {
            try {
                bacnetInstance.writeLocalObject(req);
            } catch (Exception e) {
                logger.warn("Error writing to point {}", e, id);
                return;
            }
        });
    }

    @EventContractor(action = EventAction.PATCH, returnType = void.class)
    public void objectUpdated(@Param("id") String id, @Param("property") String property,
                              @Param("value") Object value) {
        bacnetInstances.forEach((s, bacnetInstance) -> {
            try {
                bacnetInstance.updateLocalObject(id, property, value);
            } catch (Exception e) {
                logger.warn("Error updating point {} property {}", e, id, property);
                return;
            }
        });
    }

    @EventContractor(action = EventAction.UPDATE, returnType = void.class)
    public void updateAll(@Param("points") JsonObject points) {
        bacnetInstances.forEach((s, bacnetInstance) -> bacnetInstance.initialiseLocalObjectsFromJson(points));
    }

}
