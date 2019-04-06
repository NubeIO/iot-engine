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

    private final AssertmentConsumer assertmentConsumer;

    @EventContractor(action = {EventAction.CREATE}, returnType = Single.class)
    public Single<JsonObject> create(RequestData data) {
        return runThenThrowException(data);
    }

    @EventContractor(action = {EventAction.INIT}, returnType = Single.class)
    public Single<JsonObject> init(RequestData data) {
        return runThenThrowException(data);
    }

    @EventContractor(action = {EventAction.UPDATE}, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        return runThenThrowException(data);
    }

    @EventContractor(action = {EventAction.PATCH}, returnType = Single.class)
    public Single<JsonObject> path(RequestData data) {
        return runThenThrowException(data);
    }

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE);
    }

    private Single<JsonObject> runThenThrowException(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        assertmentConsumer.accept(preResult);
        throw new EngineException("Module deployment failed");
    }

}
