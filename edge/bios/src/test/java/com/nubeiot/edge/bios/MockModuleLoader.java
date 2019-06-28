package com.nubeiot.edge.bios;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.core.PreDeploymentResult;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockModuleLoader implements EventListener {

    private final DeploymentAsserter deploymentAsserter;

    @EventContractor(action = {
        EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE, EventAction.REMOVE
    }, returnType = Single.class)
    public Single<JsonObject> sendEventMessage(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        if (Objects.nonNull(deploymentAsserter)) {
            deploymentAsserter.accept(preResult);
        }
        return Single.just(new JsonObject().put("abc", "123"));
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE,
                             EventAction.REMOVE);
    }

}
