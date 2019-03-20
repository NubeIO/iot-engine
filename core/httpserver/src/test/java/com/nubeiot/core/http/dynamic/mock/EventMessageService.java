package com.nubeiot.core.http.dynamic.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class EventMessageService extends ContainerVerticle {

    private static EventModel TEST_EVENT_MODEL = EventModel.builder()
                                                           .address("com.nubeiot.core.http.dynamic.EventMessageService")
                                                           .local(true)
                                                           .pattern(EventPattern.REQUEST_RESPONSE)
                                                           .addEvents(EventAction.GET_ONE, EventAction.GET_LIST)
                                                           .build();

    public String configFile() { return "eventService.json"; }

    @Override
    public void start() {
        super.start();
        addProvider(new MicroserviceProvider(), this::publishService);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(TEST_EVENT_MODEL, new EventBusTestHandler(TEST_EVENT_MODEL.getEvents()));
    }

    private void publishService(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("event-message-service", TEST_EVENT_MODEL.getAddress(),
                                           EventMethodDefinition.createDefault("/hey", "/hey/:id"), new JsonObject())
                    .subscribe();
    }

    @RequiredArgsConstructor
    static class EventBusTestHandler implements EventHandler {

        private final Set<EventAction> actions;

        @Override
        public @NonNull List<EventAction> getAvailableEvents() {
            return new ArrayList<>(actions);
        }

        @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
        public List<String> list() {
            return Arrays.asList("1", "2", "3");
        }

        @EventContractor(action = EventAction.GET_ONE, returnType = Integer.class)
        public int get(RequestData data) {
            return Integer.valueOf(data.body().getString("id"));
        }

    }

}
