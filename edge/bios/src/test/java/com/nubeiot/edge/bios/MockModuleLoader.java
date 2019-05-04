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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockModuleLoader implements EventHandler {

    private final AssertmentConsumer assertmentConsumer;

    @EventContractor(action = {
        EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE, EventAction.REMOVE
    }, returnType = Single.class)
    public Single<JsonObject> sendEventMessage(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        assertmentConsumer.accept(preResult);
        return Single.just(new JsonObject().put("abc", "123"));
    }

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE,
                             EventAction.REMOVE);
    }

}
