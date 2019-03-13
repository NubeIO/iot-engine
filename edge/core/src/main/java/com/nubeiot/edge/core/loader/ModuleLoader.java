package com.nubeiot.edge.core.loader;

import java.util.Arrays;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.edge.core.PreDeploymentResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ModuleLoader implements EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ModuleLoader.class);
    private final Vertx vertx;

    @EventContractor(action = {EventAction.CREATE, EventAction.INIT}, returnType = Single.class)
    public Single<JsonObject> installModule(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        logger.info("Vertx install module {}...", preResult.getServiceFQN());
        DeploymentOptions options = new DeploymentOptions(preResult.getDeployCfg().getDeployConfig()).setConfig(
            preResult.getDeployCfg().toJson());
        return vertx.rxDeployVerticle(preResult.getServiceFQN(), options).doOnError(throwable -> {
            throw new EngineException(throwable);
        }).map(id -> new JsonObject().put("deploy_id", id));
    }

    @EventContractor(action = {EventAction.REMOVE, EventAction.HALT}, returnType = Single.class)
    public Single<JsonObject> removeModule(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        String deployId = preResult.getDeployId();
        logger.info("Vertx unload module {}...", deployId);
        return vertx.rxUndeploy(deployId).onErrorResumeNext(throwable -> {
            if (!preResult.isSilent()) {
                throw new EngineException(throwable);
            }
            logger.warn("Module {} is gone in Vertx. Keep silent...", throwable, deployId);
            return CompletableObserver::onComplete;
        }).andThen(Single.just(new JsonObject().put("deploy_id", deployId)));
    }

    @EventContractor(action = {EventAction.UPDATE, EventAction.PATCH}, returnType = Single.class)
    public Single<JsonObject> reloadModule(RequestData data) {
        PreDeploymentResult preResult = JsonData.from(data.body(), PreDeploymentResult.class);
        if (preResult.getTargetState() == State.DISABLED) {
            this.removeModule(data);
        }
        logger.info("Vertx reload module {}...", preResult.getDeployId());
        return vertx.rxUndeploy(preResult.getDeployId()).onErrorResumeNext(throwable -> {
            logger.debug("Module {} is gone in Vertx. Just installing...", throwable, preResult.getDeployId());
            return CompletableObserver::onComplete;
        }).andThen(installModule(data));
    }

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.INIT, EventAction.CREATE, EventAction.UPDATE, EventAction.HALT,
                             EventAction.PATCH, EventAction.REMOVE);
    }

}
