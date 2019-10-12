package com.nubeiot.edge.installer.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.dto.PostDeploymentResult;
import com.nubeiot.edge.installer.model.dto.PreDeploymentResult;

import lombok.NonNull;

class AppDeploymentService implements DeploymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeploymentService.class);
    private final Vertx vertx;
    private final Function<String, Object> sharedDataFunc;
    private final WorkerExecutor worker;

    AppDeploymentService(@NonNull InstallerEntityHandler entityHandler) {
        this.vertx = entityHandler.vertx();
        this.sharedDataFunc = entityHandler::sharedData;
        this.worker = vertx.createSharedWorkerExecutor("installer", 1, 3, TimeUnit.MINUTES);
    }

    @EventContractor(action = {EventAction.CREATE, EventAction.INIT})
    public JsonObject install(RequestData data) {
        final PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        LOGGER.info("INSTALLER deploy physical app module {}...", preResult.getServiceFQN());
        worker.<String>executeBlocking(future -> doDeploy(preResult, future), false,
                                       result -> publishResult(preResult, result));
        return new JsonObject();
    }

    @EventContractor(action = {EventAction.REMOVE, EventAction.HALT}, returnType = Single.class)
    public JsonObject remove(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        String deployId = preResult.getDeployId();
        LOGGER.info("INSTALLER un-deploy physical app module {}...", deployId);
        worker.<String>executeBlocking(future -> doUnDeploy(preResult, preResult.isSilent(), future), false,
                                       result -> publishResult(preResult, result));
        return new JsonObject();
    }

    @EventContractor(action = {EventAction.UPDATE, EventAction.PATCH})
    public JsonObject reload(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        if (preResult.getTargetState() == State.DISABLED) {
            return remove(data);
        }
        String deployId = preResult.getDeployId();
        LOGGER.info("INSTALLER reload physical app module {}...", deployId);
        worker.<String>executeBlocking(future -> doUnDeploy(preResult, true, future), false, result -> install(data));
        return new JsonObject();
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.INIT, EventAction.CREATE, EventAction.UPDATE, EventAction.HALT,
                             EventAction.PATCH, EventAction.REMOVE);
    }

    void doDeploy(PreDeploymentResult preResult, Future<String> future) {
        final JsonObject config = NubeConfig.create(preResult.getSystemConfig(), preResult.getAppConfig()).toJson();
        final DeploymentOptions options = new DeploymentOptions().setConfig(config);
        vertx.deployVerticle(preResult.getServiceFQN(), options, future);
    }

    void doUnDeploy(PreDeploymentResult preResult, boolean silent, Future<String> future) {
        final String deployId = preResult.getDeployId();
        vertx.undeploy(deployId, async -> {
            if (async.failed()) {
                if (!silent) {
                    future.fail(new EngineException(async.cause()));
                    return;
                }
                LOGGER.warn("App module {} with deploy id {} is gone in system", async.cause(),
                            preResult.getServiceId(), deployId);
            }
            future.complete(deployId);
        });
    }

    private void publishResult(PreDeploymentResult preResult, AsyncResult<String> async) {
        final EventController client = sharedData(SharedDataDelegate.SHARED_EVENTBUS);
        final AppDeployer deployer = sharedData(InstallerEntityHandler.SHARED_APP_DEPLOYER_CFG);
        final JsonObject error = async.succeeded() ? new JsonObject() : ErrorMessage.parse(async.cause()).toJson();
        final PostDeploymentResult pr = PostDeploymentResult.from(preResult, async.result(), error);
        client.request(DeliveryEvent.from(deployer.getTrackerEvent(), new JsonObject().put("result", pr.toJson())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D> D sharedData(String dataKey) {
        return (D) sharedDataFunc.apply(dataKey);
    }

}
