package com.nubeiot.core.http.dynamic.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.NotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

class MockEventServiceListener {

    static EventModel TEST_EVENT_1 = EventModel.builder()
                                               .address("test.MockEventMessageService.1")
                                               .local(true)
                                               .pattern(EventPattern.REQUEST_RESPONSE)
                                               .addEvents(EventAction.GET_ONE, EventAction.GET_LIST)
                                               .build();
    static final SimpleEventListener TEST_EVENT_LISTENER_1 = new SimpleEventListener(TEST_EVENT_1.getEvents());
    static EventModel TEST_EVENT_2 = EventModel.clone(TEST_EVENT_1, "test.MockEventMessageService.2");
    static final MultiParamEventListener TEST_EVENT_LISTENER_2 = new MultiParamEventListener(TEST_EVENT_2.getEvents());
    static EventModel TEST_EVENT_3 = EventModel.clone(TEST_EVENT_1, "test.MockEventMessageService.3");
    static final MultiParamNotUseRequestDataEventListener TEST_EVENT_LISTENER_3
        = new MultiParamNotUseRequestDataEventListener(TEST_EVENT_3.getEvents());
    static EventModel TEST_EVENT_4 = EventModel.clone(TEST_EVENT_1, "test.MockEventMessageService.4");
    static final MultiApiPathEventListener TEST_EVENT_LISTENER_4 = new MultiApiPathEventListener(
        TEST_EVENT_4.getEvents());


    @RequiredArgsConstructor
    static class SimpleEventListener implements EventListener {

        private final Set<EventAction> actions;

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() { return new ArrayList<>(actions); }

        @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
        public List<String> list() { return Arrays.asList("1", "2", "3"); }

        @EventContractor(action = EventAction.GET_ONE, returnType = Integer.class)
        public int get(RequestData data) { return Integer.parseInt(data.body().getString("id")); }

    }


    @RequiredArgsConstructor
    static class MultiParamEventListener implements EventListener {

        private final Set<EventAction> actions;

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() { return new ArrayList<>(actions); }

        @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
        public List<String> list(RequestData data) { return Collections.singletonList(data.body().getString("cId")); }

        @EventContractor(action = EventAction.GET_ONE)
        public JsonObject get(RequestData data) { return data.body(); }

    }


    @RequiredArgsConstructor
    static class MultiApiPathEventListener implements EventListener {

        private static final JsonObject CID_01 = new JsonObject().put("cId.01", new JsonArray().add(
            new JsonObject().put("pId.01", "xxx")).add(new JsonObject().put("pId.02", "abc")));
        private static final JsonObject CID_02 = new JsonObject().put("cId.02", new JsonArray().add(
            new JsonObject().put("pId.03", "123")).add(new JsonObject().put("pId.04", "456")));
        private static final JsonArray DATA = new JsonArray().add(CID_01).add(CID_02);
        private final Set<EventAction> actions;

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() { return new ArrayList<>(actions); }

        @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
        public List<Object> list(RequestData data) {
            final String cId = data.body().getString("cId");
            return DATA.stream()
                       .map(JsonObject.class::cast)
                       .filter(s -> Strings.isBlank(cId) || s.containsKey(cId))
                       .map(JsonObject::stream)
                       .flatMap(s -> s.map(Entry::getValue))
                       .map(JsonArray.class::cast)
                       .flatMap(JsonArray::stream)
                       .collect(Collectors.toList());
        }

        @EventContractor(action = EventAction.GET_ONE)
        public JsonObject get(RequestData data) {
            final String cId = data.body().getString("cId");
            final String pId = data.body().getString("pId");
            return DATA.stream()
                       .map(JsonObject.class::cast)
                       .filter(c -> Strings.isBlank(cId) || c.containsKey(cId))
                       .map(JsonObject::stream)
                       .flatMap(s -> s.map(Entry::getValue))
                       .map(JsonArray.class::cast)
                       .flatMap(JsonArray::stream)
                       .map(JsonObject.class::cast)
                       .filter(p -> !Strings.isBlank(pId) && p.containsKey(pId))
                       .findFirst().orElseThrow(() -> new NotFoundException("Not found"));
        }

    }


    @RequiredArgsConstructor
    static class MultiParamNotUseRequestDataEventListener implements EventListener {

        private final Set<EventAction> actions;

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() { return new ArrayList<>(actions); }

        @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
        public List<String> list(@Param("xId") String xId) {
            return Collections.singletonList(xId);
        }

        @EventContractor(action = EventAction.GET_ONE)
        public JsonObject get(@Param("xId") String xId, @Param("yId") String yId) {
            return new JsonObject().put("xId", xId).put("yId", yId);
        }

    }

}
