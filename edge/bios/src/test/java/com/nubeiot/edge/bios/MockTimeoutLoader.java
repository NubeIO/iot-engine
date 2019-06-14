package com.nubeiot.edge.bios;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.edge.core.PreDeploymentResult;

public class MockTimeoutLoader implements EventHandler {

    @EventContractor(action = {EventAction.PATCH}, returnType = Single.class)
    public Single<JsonObject> sendEventMessage(RequestData data) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Single.just(new JsonObject().put("abc", "123"));
    }

    @EventContractor(action = {EventAction.CREATE}, returnType = Single.class)
    public Single<JsonObject> timeoutExceed(RequestData data) {
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Single.just(new JsonObject().put("abc", "123"));
    }

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.PATCH, EventAction.CREATE);
    }

}
