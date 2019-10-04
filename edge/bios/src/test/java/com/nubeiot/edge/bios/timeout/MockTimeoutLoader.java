package com.nubeiot.edge.bios.timeout;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;

public class MockTimeoutLoader implements EventListener {

    @EventContractor(action = {EventAction.PATCH}, returnType = Single.class)
    public Single<JsonObject> sendEventMessage(RequestData data) {
        return Single.just(new JsonObject().put("abc", "123")).delay(1, TimeUnit.SECONDS);
    }

    @EventContractor(action = {EventAction.CREATE}, returnType = Single.class)
    public Single<JsonObject> timeoutExceed(RequestData data) {
        return Single.just(new JsonObject().put("abc", "123")).delay(9, TimeUnit.SECONDS);
    }

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.PATCH, EventAction.CREATE);
    }

}
