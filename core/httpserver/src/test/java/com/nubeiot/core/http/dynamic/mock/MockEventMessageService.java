package com.nubeiot.core.http.dynamic.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class MockEventMessageService extends ContainerVerticle {

    private static EventModel TEST_EVENT_MODEL_1 = EventModel.builder()
                                                             .address("test.MockEventMessageService.1")
                                                             .local(true)
                                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                                             .addEvents(EventAction.GET_ONE, EventAction.GET_LIST)
                                                             .build();
    private static EventModel TEST_EVENT_MODEL_2 = EventModel.clone(TEST_EVENT_MODEL_1,
                                                                    "test.MockEventMessageService.2");
    private static EventModel TEST_EVENT_MODEL_3 = EventModel.clone(TEST_EVENT_MODEL_1,
                                                                    "test.MockEventMessageService.3");

    public String configFile() { return "eventService.json"; }

    @Override
    public void start() {
        super.start();
        addProvider(new MicroserviceProvider(), this::publishService);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(TEST_EVENT_MODEL_1, new EventBusTestListener(TEST_EVENT_MODEL_1.getEvents()));
        controller.register(TEST_EVENT_MODEL_2, new EventBusMultiParamTestListener(TEST_EVENT_MODEL_2.getEvents()));
        controller.register(TEST_EVENT_MODEL_3,
                            new EventBusNotUseRequestDataTestListener(TEST_EVENT_MODEL_3.getEvents()));
    }

    private void publishService(MicroContext microContext) {
        final ServiceDiscoveryController localController = microContext.getLocalController();
        localController.addEventMessageRecord("test-ems-1", TEST_EVENT_MODEL_1.getAddress(),
                                              EventMethodDefinition.createDefault("/hey", "/:id")).subscribe();
        localController.addEventMessageRecord("test-ems-2", TEST_EVENT_MODEL_2.getAddress(),
                                              EventMethodDefinition.createDefault("/c/:cId/p", "/:pId")).subscribe();
        localController.addEventMessageRecord("test-ems-3", TEST_EVENT_MODEL_3.getAddress(),
                                              EventMethodDefinition.createDefault("/x/:xId/y", "/:yId", false))
                       .subscribe();
    }

    @RequiredArgsConstructor
    static class EventBusTestListener implements EventListener {

        private final Set<EventAction> actions;

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() { return new ArrayList<>(actions); }

        @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
        public List<String> list() { return Arrays.asList("1", "2", "3"); }

        @EventContractor(action = EventAction.GET_ONE, returnType = Integer.class)
        public int get(RequestData data) { return Integer.valueOf(data.body().getString("id")); }

    }


    @RequiredArgsConstructor
    static class EventBusMultiParamTestListener implements EventListener {

        private final Set<EventAction> actions;

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() { return new ArrayList<>(actions); }

        @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
        public List<String> list(RequestData data) { return Collections.singletonList(data.body().getString("cId")); }

        @EventContractor(action = EventAction.GET_ONE)
        public JsonObject get(RequestData data) { return data.body(); }

    }


    @RequiredArgsConstructor
    static class EventBusNotUseRequestDataTestListener implements EventListener {

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
