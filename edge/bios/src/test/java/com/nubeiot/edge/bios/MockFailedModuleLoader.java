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
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.edge.core.PreDeploymentResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockFailedModuleLoader implements EventHandler {

    private final DeploymentAsserter deploymentAsserter;

    @EventContractor(action = {
        EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE
    }, returnType = Single.class)
    public Single<JsonObject> runThenThrowException(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        deploymentAsserter.accept(preResult);
        throw new EngineException("Module deployment failed");
    }

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE);
    }

}
