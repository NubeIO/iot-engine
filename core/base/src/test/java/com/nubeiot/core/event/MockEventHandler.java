package com.nubeiot.core.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.nubeiot.core.dto.RequestData;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class MockEventHandler implements EventHandler {

    public static class MockEventUnsupportedHandler implements EventHandler {

        @Override
        public List<EventType> getAvailableEvents() {
            return new ArrayList<>();
        }

    }

    @Override
    public List<EventType> getAvailableEvents() {
        return Arrays.asList(EventType.values());
    }

    @EventContractor(events = EventType.UPDATE)
    public JsonObject throwException(RequestData data) {
        throw new RuntimeException("Throw");
    }

    @EventContractor(events = {EventType.HALT, EventType.REMOVE})
    public JsonObject delete(RequestData data) {
        return new JsonObject().put("key", "delete");
    }

    @EventContractor(events = EventType.CREATE, returnType = Map.class)
    public Map<String, String> customOutputObject(RequestData data) {
        return Collections.singletonMap("key", "install");
    }

    @EventContractor(events = EventType.GET_LIST, returnType = Single.class)
    public Single<Map<String, String>> customOutputSingleObject(RequestData data) {
        return Single.just(Collections.singletonMap("key", "list"));
    }

    @EventContractor(events = EventType.INIT, returnType = Single.class)
    public Single<JsonObject> customOutputSingleJson(RequestData data) {
        return Single.just(new JsonObject().put("key", "init"));
    }

    public JsonObject publicNoContractor(RequestData data) {
        return new JsonObject().put("key", "Public method without contractor");
    }

    @EventContractor(events = EventType.GET_ONE)
    public static JsonObject staticForFun(RequestData data) {
        return new JsonObject().put("key", "Static method with contractor");
    }

    @EventContractor(events = EventType.GET_ONE)
    public void publicNotValidOutput(RequestData data) {
        new JsonObject().put("key", "Public method with not valid output");
    }

    @EventContractor(events = EventType.GET_ONE)
    public JsonObject publicNoInput() {
        return new JsonObject().put("key", "Public method with no input");
    }

    @EventContractor(events = EventType.GET_ONE)
    JsonObject nonPublicWithContractor(RequestData data) {
        return new JsonObject().put("key", "Non Public method with contractor");
    }

}
