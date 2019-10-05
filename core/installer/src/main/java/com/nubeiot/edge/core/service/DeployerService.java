package com.nubeiot.edge.core.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.reactivex.CompletableObserver;
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
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.edge.core.PreDeploymentResult;

import lombok.NonNull;

class DeployerService implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployerService.class);
    private final Vertx vertx;
    private final Function<String, Object> sharedDataFunc;
    private final EventModel postEvent;
    private final WorkerExecutor worker;

    public DeployerService(Vertx vertx, Function<String, Object> sharedDataFunc, EventModel postEvent) {
        this.vertx = vertx;
        this.sharedDataFunc = sharedDataFunc;
        this.postEvent = postEvent;
        this.worker = vertx.createSharedWorkerExecutor("installer", 3, 15, TimeUnit.MINUTES);
    }

    @EventContractor(action = {EventAction.UNKNOWN}, returnType = Single.class)
    public Single<JsonObject> installModule(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        LOGGER.info("Vertx install module {}...", preResult.getServiceFQN());
        DeploymentOptions options = new DeploymentOptions().setConfig(
            NubeConfig.create(preResult.getSystemConfig(), preResult.getAppConfig()).toJson());
        return io.vertx.reactivex.core.Vertx.newInstance(vertx)
                                            .rxDeployVerticle(preResult.getServiceFQN(), options)
                                            .doOnError(throwable -> {
                                                throw new EngineException(throwable);
                                            })
                                            .map(id -> new JsonObject().put("deploy_id", id));
    }

    @EventContractor(action = {EventAction.REMOVE, EventAction.HALT}, returnType = Single.class)
    public Single<JsonObject> removeModule(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        String deployId = preResult.getDeployId();
        LOGGER.info("Vertx unload module {}...", deployId);
        return io.vertx.reactivex.core.Vertx.newInstance(vertx).rxUndeploy(deployId).onErrorResumeNext(throwable -> {
            if (!preResult.isSilent()) {
                throw new EngineException(throwable);
            }
            LOGGER.warn("Module {} is gone in Vertx. Keep silent...", throwable, deployId);
            return CompletableObserver::onComplete;
        }).andThen(Single.just(new JsonObject().put("deploy_id", deployId)));
    }

    @EventContractor(action = {EventAction.UPDATE, EventAction.PATCH}, returnType = Single.class)
    public Single<JsonObject> reloadModule(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        if (preResult.getTargetState() == State.DISABLED) {
            this.removeModule(data);
        }
        LOGGER.info("Vertx reload module {}...", preResult.getDeployId());
        return io.vertx.reactivex.core.Vertx.newInstance(vertx)
                                            .rxUndeploy(preResult.getDeployId())
                                            .onErrorResumeNext(throwable -> {
                                                LOGGER.debug("Module {} is gone in Vertx. Just installing...",
                                                             throwable, preResult.getDeployId());
                                                return CompletableObserver::onComplete;
                                            })
                                            .andThen(installModule(data));
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.INIT, EventAction.CREATE, EventAction.UPDATE, EventAction.HALT,
                             EventAction.PATCH, EventAction.REMOVE);
    }

    @EventContractor(action = {EventAction.CREATE, EventAction.INIT}, returnType = Boolean.class)
    public boolean installV2(RequestData data) {
        final PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        LOGGER.info("Vertx install module {}...", preResult.getServiceFQN());
        worker.executeBlocking(future -> doDeploy(preResult, future), false,
                               result -> publishResult(preResult, result));
        return true;
    }

    void doDeploy(PreDeploymentResult preResult, Future<Object> future) {
        final JsonObject config = NubeConfig.create(preResult.getSystemConfig(), preResult.getAppConfig()).toJson();
        final DeploymentOptions options = new DeploymentOptions().setConfig(config);
        vertx.deployVerticle(preResult.getServiceFQN(), options, res -> future.handle(res.map(s -> s)));
    }

    private void publishResult(PreDeploymentResult preResult, AsyncResult<Object> result) {
        final EventController eventClient = (EventController) sharedDataFunc.apply(SharedDataDelegate.SHARED_EVENTBUS);
        JsonObject error = result.succeeded() ? null : ErrorMessage.parse(result.cause()).toJson();
        final JsonObject payload = new JsonObject().put("result", preResult.toJson()
                                                                           .put("deploy_id", result.result())
                                                                           .put("error", error));
        eventClient.request(DeliveryEvent.from(postEvent, EventAction.MONITOR, payload));
    }

}
