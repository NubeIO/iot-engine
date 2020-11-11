package com.nubeiot.core.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.mock.MockChild;
import com.nubeiot.core.event.mock.MockParent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public class MockEventListener implements EventListener {

    @EventContractor(action = EventAction.GET_ONE)
    public static JsonObject staticForFun(RequestData data) {
        return new JsonObject().put("key", "Static method with contractor");
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
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

    @EventContractor(action = EventAction.PATCH, returnType = void.class)
    public void publicNotValidOutput(RequestData data) {
        new JsonObject().put("key", "Public method with not valid output");
    }

    @EventContractor(action = EventAction.RETURN)
    public JsonObject publicNoInput() {
        return new JsonObject().put("key", "Public method with no input");
    }

    @EventContractor(action = EventAction.RETURN)
    public JsonObject secondPublicNoInput() {
        return new JsonObject().put("key", "Second public method with no input");
    }

    @EventContractor(action = EventAction.MIGRATE)
    JsonObject nonPublicWithContractor(RequestData data) {
        return new JsonObject().put("key", "Non Public method with contractor");
    }

    public static class MockEventUnsupportedListener implements EventListener {

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
            return new ArrayList<>();
        }

    }


    public static class MockChildEventListener extends MockEventListener {

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
            return Collections.singletonList(EventAction.CREATE);
        }

    }


    public static class MockEventWithDiffParam implements EventListener {

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
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

        @EventContractor(action = EventAction.HALT)
        public JsonObject collectionParam(@Param("list") Collection<String> data) {
            JsonObject result = new JsonObject();
            data.forEach(item -> {
                result.put(item, item);
            });
            return result;
        }

        @EventContractor(action = EventAction.RETURN, returnType = Single.class)
        public JsonObject wrongReturnType() {
            return new JsonObject();
        }

        @EventContractor(action = EventAction.INIT, returnType = MockChild.class)
        public MockParent annotatedExtendsReturnType() {
            return new MockParent();
        }

        @EventContractor(action = EventAction.MIGRATE, returnType = MockParent.class)
        public MockChild returnTypeExtendsAnnotated() {
            return new MockChild();
        }

        @EventContractor(action = EventAction.UNKNOWN)
        public JsonObject refParam(@Param("metadata") JsonObject data) { return data; }

    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    static class MockParam {

        private int id;
        private String name;

    }

}
