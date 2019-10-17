package com.nubeiot.core.http.mock;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

public class MockEventBusErrorListener extends MockEventBusListener {

    private MockEventBusErrorListener(EventBus eventBus, String address) {
        super(eventBus, address);
    }

    public static MockEventBusListener create(EventBus eventBus, String address) {
        return new MockEventBusErrorListener(eventBus, address);
    }

    public static MockEventBusListener create(EventBus eventBus) {
        return new MockEventBusErrorListener(eventBus, "http.server.test");
    }

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject list(RequestData data) {
        throw new RuntimeException("xxx");
    }

    @EventContractor(action = EventAction.CREATE)
    public JsonObject create(RequestData data) {
        throw new EngineException("Engine error");
    }

    @EventContractor(action = EventAction.UPDATE)
    public JsonObject update(RequestData data) {
        throw new NubeException(ErrorCode.INVALID_ARGUMENT, "invalid");
    }

}
