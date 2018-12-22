package com.nubeiot.core.http.mock;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;

public class MockEventBusErrorHandler extends MockEventBusHandler {

    private MockEventBusErrorHandler(EventBus eventBus, String address) {
        super(eventBus, address);
    }

    public static MockEventBusHandler create(EventBus eventBus, String address) {
        return new MockEventBusErrorHandler(eventBus, address);
    }

    public static MockEventBusHandler create(EventBus eventBus) {
        return new MockEventBusErrorHandler(eventBus, "http.server.test");
    }

    @EventContractor(events = EventAction.GET_LIST)
    public JsonObject list(RequestData data) {
        throw new RuntimeException("xxx");
    }

    @EventContractor(events = EventAction.CREATE)
    public JsonObject create(RequestData data) {
        throw new EngineException("Engine error");
    }

    @EventContractor(events = EventAction.UPDATE)
    public JsonObject update(RequestData data) {
        throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "invalid");
    }

}
