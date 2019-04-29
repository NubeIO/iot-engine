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

public class MockDeploymentHandler implements EventHandler {

    @EventContractor(action = {EventAction.CREATE}, returnType = Single.class)
    public Single<JsonObject> create(RequestData data) {
        return sendEventMessage(data);
    }

    @EventContractor(action = {EventAction.INIT}, returnType = Single.class)
    public Single<JsonObject> init(RequestData data) {
        return sendEventMessage(data);
    }

    @EventContractor(action = {EventAction.UPDATE}, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        return sendEventMessage(data);
    }

    @EventContractor(action = {EventAction.PATCH}, returnType = Single.class)
    public Single<JsonObject> path(RequestData data) {
        return sendEventMessage(data);
    }

    @EventContractor(action = {EventAction.REMOVE}, returnType = Single.class)
    public Single<JsonObject> remove(RequestData data) {
        return sendEventMessage(data);
    }

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE,
                             EventAction.REMOVE);
    }

    private Single<JsonObject> sendEventMessage(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        return Single.just(new JsonObject().put("abc", "123"));
    }

}
