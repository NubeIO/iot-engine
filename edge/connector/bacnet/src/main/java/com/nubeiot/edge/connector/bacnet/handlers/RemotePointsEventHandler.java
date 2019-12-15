package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
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
@Deprecated
@RequiredArgsConstructor
public class RemotePointsEventHandler implements EventListener {

    private final Map<String, BACnetInstance> bacnetInstances;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.POINTS.getEvents()));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> readRemoteDevicePointValue(RequestData requestData) {
        String network = requestData.body().getString("network");
        String instanceNumber = requestData.body().getString("deviceId");
        String objectId = requestData.body().getString("objectId");

        try {
            return bacnetInstances.get(network).readRemoteObjectvalue(Integer.parseInt(instanceNumber), objectId);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> readRemoteDeviceMultiplePointsValue(RequestData requestData) {
        String network = requestData.body().getString("network");
        String instanceNumber = requestData.body().getString("deviceId");
        String oidStr = requestData.getFilter().getString("objectIds");

        try {
            ArrayList<String> objectIds = new ArrayList<>(Arrays.asList(oidStr.split(",")));
            return bacnetInstances.get(network)
                                  .readMultipleRemoteObjectvalue(Integer.parseInt(instanceNumber), objectIds);
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> writeRemoteDevicePointValue(RequestData requestData) {
        String network = requestData.body().getString("network");
        String instanceNumber = requestData.body().getString("deviceId");
        String objectId = requestData.body().getString("objectId");
        String priority = requestData.body().getString("priority");
        Object obj = requestData.body().getValue("value");

        if (priority == null || priority.isEmpty()) {
            priority = "16";
        }

        try {
            return bacnetInstances.get(network)
                                  .writeAtPriority(Integer.parseInt(instanceNumber), objectId, obj,
                                                   Integer.parseInt(priority));
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

    //@EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    //    public Single<JsonObject> readRemoteDevicePointValue(@Param("network") String network,
    //                                                         @Param("deviceId") String instanceNumber,
    //                                                         @Param("objectId") String objectId) {
    //
    //        try {
    //            return bacnetInstances.get(network).readRemoteObjectvalue(Integer.parseInt(instanceNumber), objectId);
    //        } catch (NullPointerException e) {
    //            return Single.error(new BACnetException("No network found", e));
    //        } catch (ClassCastException e) {
    //            return Single.error(e);
    //        }
    //    }
    //
    //    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    //    public Single<JsonObject> readRemoteDeviceMultiplePointsValue(@Param("network") String network,
    //                                                                  @Param("deviceId") String instanceNumber,
    //                                                                  @Param("objectIds") String objectIds) {
    //
    //        try {
    //            return bacnetInstances.get(network).readMultipleRemoteObjectvalue(Integer.parseInt
    // (instanceNumber), objectIds.split(","));
    //        } catch (NullPointerException e) {
    //            return Single.error(new BACnetException("No network found", e));
    //        } catch (ClassCastException e) {
    //            return Single.error(e);
    //        }
    //    }
    //
    //    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    //    public Single<JsonObject> writeRemoteDevicePointValue(@Param("network") String network,
    //                                                          @Param("deviceId") String instanceNumber,
    //                                                          @Param("objectId") String objectId,
    //                                                          @Param("priority") String priority,
    //                                                          @Param("value") Object obj) {
    //
    //        try {
    //            return bacnetInstances.get(network)
    //                                  .writeAtPriority(Integer.parseInt(instanceNumber), objectId, obj,
    //                                                   Integer.parseInt(priority));
    //        } catch (NullPointerException e) {
    //            return Single.error(new BACnetException("No network found", e));
    //        } catch (ClassCastException e) {
    //            return Single.error(e);
    //        }
    //    }
}
