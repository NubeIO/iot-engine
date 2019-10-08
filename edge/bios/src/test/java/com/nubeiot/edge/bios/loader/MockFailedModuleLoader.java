package com.nubeiot.edge.bios.loader;

import java.util.Arrays;
import java.util.Collection;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.edge.installer.model.dto.PreDeploymentResult;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockFailedModuleLoader implements EventListener {

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
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE);
    }

}
