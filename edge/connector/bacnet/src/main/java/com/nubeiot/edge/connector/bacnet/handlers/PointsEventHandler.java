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
import com.serotonin.bacnet4j.exception.ErrorAPDUException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.Real;

import lombok.Getter;

/*
 * VERTX event bus message handler
 *  calls respective messages in BACnetInstance
 */


public class PointsEventHandler implements EventHandler {

    private Map<String, BACnetInstance> bacnetInstances;

    @Getter
    private final List<EventAction> availableEvents;

    public PointsEventHandler(Map bacnetInstances) {
        this.bacnetInstances = bacnetInstances;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.POINTS.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getRemoteDevicePoints(@Param("network") String network,
                                                    @Param("deviceId") int instanceNumber) {
        try {
            return bacnetInstances.get(network).getRemoteDeviceObjectList(instanceNumber);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getRemoteDevicePointExtended(@Param("network") String network,
                                                           @Param("deviceId") int instanceNumber,
                                                           @Param("objectId") String objectID) {
        try {
            return bacnetInstances.get(network).getRemoteObjectProperties(instanceNumber, objectID);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    //    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    //    public Single<JsonObject> saveRemoteDevicePoint(@Param("network") String network,
    //                                                    @Param("deviceId") int instanceNumber,
    //                                                    @Param("objectId") String objectID) {
    //        return saveRemoteDevicePoint(network, instanceNumber, objectID, 0);
    //    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> saveRemoteDevicePoint(@Param("network") String network,
                                                    @Param("deviceId") int instanceNumber,
                                                    @Param("objectId") String objectID,
                                                    @Param("pollSeconds") int poll) {
        try {
            return bacnetInstances.get(network)
                                  .remoteObjectSubscribeCOV(instanceNumber, objectID)
                                  .flatMap(entries -> Single.just(new JsonObject().put("saveType", "COV")))
                                  .onErrorResumeNext(throwable -> {
                                      if (throwable instanceof ErrorAPDUException) {
                                          //TODO: polling
                                          return Single.just(new JsonObject().put("saveType", "POLL"));
                                      }
                                      return Single.error(throwable);
                                  });
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    //    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    //    public Single<JsonObject> removeRemoteDevicePoint(@Param("network") String network,
    //                                                      @Param("deviceId") int instanceNumber,
    //                                                      @Param("objectId") String objectID) {
    //
    //    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> writeRemoteDevicePointValue(@Param("network") String network,
                                                          @Param("deviceId") int instanceNumber,
                                                          @Param("objectId") String objectID,
                                                          @Param("priority") int priority, @Param("value") Object obj) {
        try {
            return writeRemoteDevicePointValueString(network, instanceNumber, objectID, priority, obj.toString());
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    public Single<JsonObject> writeRemoteDevicePointValueString(@Param("network") String network,
                                                                @Param("deviceId") int instanceNumber,
                                                                @Param("objectId") String objectID,
                                                                @Param("priority") int priority,
                                                                @Param("value") String str) {
        Encodable val;
        if (priority < 1 || priority > 16) {
            return Single.error(new BACnetException("Invalid priority array index"));
        }

        if (str == null || str.equalsIgnoreCase("null")) {
            val = Null.instance;
        } else if (str.equalsIgnoreCase("true")) {
            val = BinaryPV.active;
        } else if (str.equalsIgnoreCase("false")) {
            val = BinaryPV.inactive;
        } else {
            try {
                val = new Real(Float.parseFloat(str));
            } catch (NumberFormatException e) {
                return Single.error(e);
            }
        }
        try {
            return bacnetInstances.get(network).writeAtPriority(instanceNumber, objectID, val, priority);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

}
