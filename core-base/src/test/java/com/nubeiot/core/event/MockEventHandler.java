package com.nubeiot.core.event;

import java.util.Arrays;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class MockEventHandler extends EventHandler {

    @Override
    protected List<EventType> getAvailableEvents() {
        return Arrays.asList(EventType.INIT, EventType.CREATE, EventType.UPDATE, EventType.HALT, EventType.REMOVE,
                             EventType.GET_ONE);
    }

    @EventContractor(values = EventType.UPDATE)
    private Single<JsonObject> throwException(RequestData data) {
        throw new NubeException("Throw");
    }

    @EventContractor(values = EventType.CREATE)
    private Single<JsonObject> install(RequestData data) {
        return Single.just(new JsonObject().put("key", "install"));
    }

    @EventContractor(values = {EventType.HALT, EventType.REMOVE})
    private Single<JsonObject> delete(RequestData data) {
        return Single.just(new JsonObject().put("key", "delete"));
    }

    private void privateForFun() {
        System.out.println("Private method without contractor");
    }

    @EventContractor(values = EventType.INIT)
    private static void staticForFun() {
        System.out.println("Static method with contractor");
    }

    @EventContractor(values = EventType.GET_ONE)
    public void publicForFun() {
        System.out.println("Public method with contractor");
    }

}
