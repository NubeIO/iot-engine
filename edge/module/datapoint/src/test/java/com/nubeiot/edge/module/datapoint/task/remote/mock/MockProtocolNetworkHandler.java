package com.nubeiot.edge.module.datapoint.task.remote.mock;

import java.util.Collection;
import java.util.Collections;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MockProtocolNetworkHandler implements EventListener {

    private final JsonObject metadata;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singleton(EventAction.CREATE);
    }

    @EventContractor(action = EventAction.CREATE)
    public JsonObject create(RequestData requestData) {
        return requestData.body().put("metadata", metadata);
    }

}
