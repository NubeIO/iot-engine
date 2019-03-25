package com.nubeiot.core.http.mock;

import java.util.Arrays;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;

public class MockEventBusSuccessHandler extends MockEventBusHandler {

    private MockEventBusSuccessHandler(EventBus eventBus, String address) {
        super(eventBus, address);
    }

    public static MockEventBusHandler create(EventBus eventBus, String address) {
        return new MockEventBusSuccessHandler(eventBus, address);
    }

    public static MockEventBusHandler create(EventBus eventBus) {
        return new MockEventBusSuccessHandler(eventBus, "http.server.test");
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
    public List<String> list(RequestData data) {
        return Arrays.asList("1", "2", "3");
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Integer.class)
    public int get(RequestData data) {
        return Integer.valueOf(data.body().getString("event_id"));
    }

    @EventContractor(action = EventAction.CREATE)
    public JsonObject create(RequestData data) {
        return new JsonObject().put("create", "success");
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<String> update(RequestData data) {
        return Single.just("success");
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData data) {
        return Single.just(new JsonObject().put("patch", "success")
                                           .put("event_id", Integer.valueOf(data.body().getString("event_id"))));
    }

}
