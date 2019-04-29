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
public class PointsEventHandler implements EventHandler {

    private final Map<String, BACnetInstance> bacnetInstances;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.POINTS.getEvents()));
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
                                                           @Param("objectId") String objectId) {
        try {
            return bacnetInstances.get(network).getRemoteObjectProperties(instanceNumber, objectId);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> writeRemoteDevicePointValue(@Param("network") String network,
                                                          @Param("deviceId") int instanceNumber,
                                                          @Param("objectId") String objectId,
                                                          @Param("priority") int priority, @Param("value") Object obj) {
        Encodable val;
        if (priority < 1 || priority > 16) {
            return Single.error(new BACnetException("Invalid priority array index"));
        }
        try {
            return bacnetInstances.get(network).writeAtPriority(instanceNumber, objectId, obj, priority);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    //    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    //    public Single<JsonObject> saveRemoteDevicePoint(@Param("network") String network,
    //                                                    @Param("deviceId") int instanceNumber,
    //                                                    @Param("objectId") String objectId,
    //                                                    @Param("pollSeconds") long poll) {
    //        try {
    //            return bacnetInstances.get(network)
    //                                  .remoteObjectSubscribeCOV(instanceNumber, objectId)
    //                                  .flatMap(entries -> Single.just(new JsonObject().put("saveType", "COV")))
    //                                  .onErrorResumeNext(throwable -> {
    //                                      if (throwable instanceof ErrorAPDUException) {
    //                                          bacnetInstances.get(network)
    //                                                         .initRemoteObjectPolling(instanceNumber, objectId, poll)
    //                                                         .onErrorResumeNext(throwable1 -> Single.error
    //                                                         (throwable1));
    //                                          return Single.just(new JsonObject().put("saveType", "POLL"));
    //                                      }
    //                                      return Single.error(throwable);
    //                                  });
    //        } catch (NullPointerException e) {
    //            return Single.error(new BACnetException("No network found", e));
    //        }
    //    }
    //
    //    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    //    public Single<JsonObject> removeRemoteDevicePoint(@Param("network") String network,
    //                                                      @Param("deviceId") int instanceNumber,
    //                                                      @Param("objectId") String objectId) {
    //        try {
    //            bacnetInstances.get(network).removeRemoteObjectSubscription(instanceNumber, objectId);
    //        } catch (NullPointerException e) {
    //            return Single.error(new BACnetException("No network found", e));
    //        }
    //    }
}
