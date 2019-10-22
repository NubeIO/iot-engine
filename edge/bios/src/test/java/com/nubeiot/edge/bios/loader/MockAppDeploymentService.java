package com.nubeiot.edge.bios.loader;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.bios.mock.MockBiosEdgeVerticle;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.dto.PostDeploymentResult;
import com.nubeiot.edge.installer.model.dto.PreDeploymentResult;
import com.nubeiot.edge.installer.service.AppDeployer;
import com.nubeiot.edge.installer.service.DeploymentService;
import com.sun.istack.internal.NotNull;

import lombok.NonNull;

public class MockAppDeploymentService implements DeploymentService {

    private final MockBiosEdgeVerticle mockBiosEdgeVerticle;
    private final DeploymentAsserter deploymentAsserter;
    private final boolean deployState;

    public MockAppDeploymentService(@NotNull MockBiosEdgeVerticle mockBiosEdgeVerticle,
                                    @NotNull DeploymentAsserter deploymentAsserter, @NotNull boolean deployState) {
        this.mockBiosEdgeVerticle = mockBiosEdgeVerticle;
        this.deploymentAsserter = deploymentAsserter;
        this.deployState = deployState;
    }

    @EventContractor(action = {
        EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE, EventAction.REMOVE
    }, returnType = Single.class)
    public Single<JsonObject> sendEventMessage(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        if (Objects.nonNull(deploymentAsserter)) {
            deploymentAsserter.accept(preResult);
        }
        this.publishResult(preResult);
        return Single.just(new JsonObject().put("abc", "123"));
    }

    private void publishResult(PreDeploymentResult preResult) {
        final EventController client = sharedData(SharedDataDelegate.SHARED_EVENTBUS);
        final AppDeployer deployer = sharedData(InstallerEntityHandler.SHARED_APP_DEPLOYER_CFG);
        JsonObject error = new JsonObject();
        if (!deployState) {
            error = error.put("error", "Module deployment failed");
        }
        final PostDeploymentResult pr = PostDeploymentResult.from(preResult, "123", error);
        client.request(DeliveryEvent.from(deployer.getTrackerEvent(), new JsonObject().put("result", pr.toJson())));
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.UPDATE, EventAction.PATCH, EventAction.INIT, EventAction.CREATE,
                             EventAction.REMOVE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D> D sharedData(String dataKey) {
        Function<String, Object> sharedDataFunc = key -> mockBiosEdgeVerticle.getEntityHandler().sharedData(key);
        return (D) sharedDataFunc.apply(dataKey);
    }

}
