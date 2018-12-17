package com.nubeiot.edge.core.loader;

import java.util.Arrays;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.edge.core.PreDeploymentResult;

import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ModuleLoader implements EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ModuleLoader.class);
    private final Vertx context;

    @EventContractor(events = {EventType.CREATE, EventType.INIT}, returnType = Single.class)
    public Single<JsonObject> installModule(RequestData data) {
        PreDeploymentResult preResult = PreDeploymentResult.fromJson(data.getBody());
        logger.info("Vertx install module {} with config {}...", preResult.getServiceId(), preResult.getDeployCfg());
        DeploymentOptions options = new DeploymentOptions().setConfig(preResult.getDeployCfg());
        return context.rxDeployVerticle(preResult.getServiceId(), options).doOnError(throwable -> {
            throw new EngineException(throwable);
        }).map(id -> new JsonObject().put("deploy_id", id));
    }

    @EventContractor(events = {EventType.REMOVE, EventType.HALT}, returnType = Single.class)
    public Single<JsonObject> removeModule(RequestData data) {
        PreDeploymentResult preResult = PreDeploymentResult.fromJson(data.getBody());
        String deployId = preResult.getDeployId();
        logger.info("Vertx unload module {}...", deployId);
        return context.rxUndeploy(deployId).onErrorResumeNext(throwable -> {
            if (!preResult.isSilent()) {
                throw new EngineException(throwable);
            }
            logger.warn("Module {} is gone in Vertx. Keep silent...", throwable, deployId);
            return CompletableObserver::onComplete;
        }).andThen(Single.just(new JsonObject().put("deploy_id", deployId)));
    }

    @EventContractor(events = EventType.UPDATE, returnType = Single.class)
    public Single<JsonObject> reloadModule(RequestData data) {
        PreDeploymentResult preResult = PreDeploymentResult.fromJson(data.getBody());
        logger.info("Vertx reload module {} with config {}...", preResult.getDeployId(), preResult.getDeployCfg());
        return context.rxUndeploy(preResult.getDeployId()).onErrorResumeNext(throwable -> {
            logger.debug("Module {} is gone in Vertx. Just installing...", throwable, preResult.getDeployId());
            return CompletableObserver::onComplete;
        }).andThen(installModule(data));
    }

    @Override
    public List<EventType> getAvailableEvents() {
        return Arrays.asList(EventType.INIT, EventType.CREATE, EventType.UPDATE, EventType.HALT, EventType.REMOVE);
    }

}
