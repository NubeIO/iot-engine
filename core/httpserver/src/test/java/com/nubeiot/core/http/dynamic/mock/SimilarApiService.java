package com.nubeiot.core.http.dynamic.mock;

import java.util.Collection;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.ServiceDiscoveryController;

import lombok.NonNull;

public class SimilarApiService extends MockEventOneApiOneLocService {

    static EventModel EVENT_1 = EventModel.builder()
                                          .address("test.SimilarApiService.1")
                                          .local(true)
                                          .pattern(EventPattern.REQUEST_RESPONSE)
                                          .addEvents(EventAction.GET_ONE, EventAction.GET_LIST)
                                          .build();
    static EventModel EVENT_2 = EventModel.clone(EVENT_1, "test.SimilarApiService.2");

    @Override
    public void registerEventbus(EventbusClient controller) {
        controller.register(EVENT_1, new MockSiteListener()).register(EVENT_2, new MockProductListener());
    }

    @Override
    protected void publishService(MicroContext microContext) {
        final ServiceDiscoveryController controller = microContext.getLocalController();
        Single.concat(controller.addEventMessageRecord("ems-5", EVENT_1.getAddress(),
                                                       EventMethodDefinition.createDefault("/client/:cId/site",
                                                                                           "/:sId")),
                      controller.addEventMessageRecord("ems-5", EVENT_2.getAddress(),
                                                       EventMethodDefinition.createDefault(
                                                           "/client/:cId/site/:sId/product", "/:pId"))).subscribe();
    }

    static class MockSiteListener implements EventListener {

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
            return ActionMethodMapping.DQL_MAP.get().keySet();
        }

        @EventContractor(action = EventAction.GET_LIST)
        public JsonObject list(RequestData data) {
            return new JsonObject().put("from", "GET_LIST From site");
        }

        @EventContractor(action = EventAction.GET_ONE)
        public JsonObject get(RequestData data) {
            return new JsonObject().put("from", "GET_ONE From site");
        }

    }


    static class MockProductListener implements EventListener {

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
            return ActionMethodMapping.DQL_MAP.get().keySet();
        }

        @EventContractor(action = EventAction.GET_LIST)
        public JsonObject list(RequestData data) {
            return new JsonObject().put("from", "GET_LIST From product");
        }

        @EventContractor(action = EventAction.GET_ONE)
        public JsonObject get(RequestData data) {
            return new JsonObject().put("from", "GET_ONE From product");
        }

    }

}
