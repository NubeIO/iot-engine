package com.nubeiot.core.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventContractor.Param;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MockEventHandler implements EventHandler {

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.values());
    }

    @EventContractor(action = EventAction.UPDATE)
    public JsonObject throwException(RequestData data) {
        throw new RuntimeException("Throw");
    }

    @EventContractor(action = {EventAction.HALT, EventAction.REMOVE})
    public JsonObject delete(RequestData data) {
        return new JsonObject().put("key", "delete");
    }

    @EventContractor(action = EventAction.CREATE, returnType = Map.class)
    public Map<String, String> customOutputObject(RequestData data) {
        return Collections.singletonMap("key", "install");
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<Map<String, String>> customOutputSingleObject(RequestData data) {
        return Single.just(Collections.singletonMap("key", "list"));
    }

    @EventContractor(action = EventAction.INIT, returnType = Single.class)
    public Single<JsonObject> customOutputSingleJson(RequestData data) {
        return Single.just(new JsonObject().put("key", "init"));
    }

    public JsonObject publicNoContractor(RequestData data) {
        return new JsonObject().put("key", "Public method without contractor");
    }

    @EventContractor(action = EventAction.GET_ONE)
    public static JsonObject staticForFun(RequestData data) {
        return new JsonObject().put("key", "Static method with contractor");
    }

    @EventContractor(action = EventAction.GET_ONE)
    public void publicNotValidOutput(RequestData data) {
        new JsonObject().put("key", "Public method with not valid output");
    }

    @EventContractor(action = EventAction.GET_ONE)
    public JsonObject publicNoInput() {
        return new JsonObject().put("key", "Public method with no input");
    }

    @EventContractor(action = EventAction.GET_ONE)
    JsonObject nonPublicWithContractor(RequestData data) {
        return new JsonObject().put("key", "Non Public method with contractor");
    }

    public static class MockEventUnsupportedHandler implements EventHandler {

        @Override
        public List<EventAction> getAvailableEvents() {
            return new ArrayList<>();
        }

    }


    public static class MockEventWithDiffParam implements EventHandler {

        @Override
        public List<EventAction> getAvailableEvents() {
            return Arrays.asList(EventAction.values());
        }

        @EventContractor(action = EventAction.GET_LIST, returnType = String.class)
        public String noParam() { return "hello"; }

        @EventContractor(action = EventAction.GET_ONE, returnType = int.class)
        public int javaParam(@Param("id") String id) {
            return Integer.parseInt(id);
        }

        @EventContractor(action = EventAction.CREATE, returnType = RequestData.class)
        public RequestData refParam(RequestData data) { return data; }

        @EventContractor(action = EventAction.PATCH, returnType = RequestData.class)
        public RequestData overrideRefParam(@Param("data") RequestData data) { return data; }

        @EventContractor(action = EventAction.UPDATE)
        public JsonObject twoRefParams(@Param("mock") MockParam param, @Param("data") RequestData data) {
            return new JsonObject().put("param", JsonObject.mapFrom(param)).put("request", data.toJson());
        }

        @EventContractor(action = EventAction.REMOVE)
        public JsonObject mixParams(@Param("id") int id, @Param("data") RequestData data) {
            return new JsonObject().put("id", id).put("request", data.toJson());
        }

    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    static class MockParam {

        private int id;
        private String name;

    }

}
