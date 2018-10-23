package com.nubeio.iot.share.event;

import java.util.Arrays;
import java.util.List;

import com.nubeio.iot.share.dto.RequestData;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class MockEventHandler extends EventHandler {

    @Override
    protected List<EventType> getAvailableEvents() {
        return Arrays.asList(EventType.CREATE, EventType.HALT, EventType.REMOVE, EventType.GET_LIST);
    }

    @EventContractor(values = EventType.CREATE)
    private Single<JsonObject> install(RequestData data) {
        return Single.just(new JsonObject().put("key", "install"));
    }

    @EventContractor(values = {EventType.HALT, EventType.REMOVE})
    private Single<JsonObject> delete(RequestData data) {
        return Single.just(new JsonObject().put("key", "delete"));
    }

    @EventContractor(values = EventType.GET_LIST)
    private Single<JsonObject> getList(RequestData data) {
        return Single.just(new JsonObject().put("key", "list"));
    }

}
