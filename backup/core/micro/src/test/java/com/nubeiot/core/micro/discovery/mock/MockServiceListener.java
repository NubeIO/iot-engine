package com.nubeiot.core.micro.discovery.mock;

import java.util.Collection;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.ActionMethodMapping;

import lombok.NonNull;

public final class MockServiceListener implements EventListener {

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.CRUD_MAP.get().keySet();
    }

    @EventContractor(action = EventAction.CREATE)
    public JsonObject create(RequestData requestData) {
        return new JsonObject().put("action", EventAction.CREATE)
                               .put(Headers.X_REQUEST_BY, requestData.headers().getString(Headers.X_REQUEST_BY));
    }

    @EventContractor(action = EventAction.UPDATE)
    public JsonObject update(RequestData requestData) {
        throw new IllegalArgumentException("hey");
    }

}
