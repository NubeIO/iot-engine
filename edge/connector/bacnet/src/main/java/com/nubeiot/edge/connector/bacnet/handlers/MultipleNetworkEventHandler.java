package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

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
@RequiredArgsConstructor
public class MultipleNetworkEventHandler implements EventListener {

    private final Map<String, BACnetInstance> bacnetInstances;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BACnetEventModels.NETWORKS_ALL.getEvents()));
    }

    //GET ALL DEVICES
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getCachedRemoteDevices() {
        try {
            JsonObject data = new JsonObject();
            return Observable.fromIterable(bacnetInstances.entrySet())
                             .map(entry -> entry.getValue()
                                                .getRemoteDevices()
                                                .flatMap(entries -> Single.just(
                                                    new JsonObject().put("network", entry.getKey())
                                                                    .put("data", entries)))
                                                .blockingGet())
                             .flatMapSingle(Single::just)
                             .collect(() -> data, (d, instanceJson) -> d.put(instanceJson.getString("network"),
                                                                             instanceJson.getValue("data")));
        } catch (NullPointerException e) {
            return Single.error(new BACnetException("No network found", e));
        }
    }

}
