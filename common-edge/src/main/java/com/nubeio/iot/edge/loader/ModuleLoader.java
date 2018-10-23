package com.nubeio.iot.edge.loader;

import java.util.Arrays;
import java.util.List;

import com.nubeio.iot.share.event.EventContractor;
import com.nubeio.iot.share.event.EventHandler;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.exceptions.EngineException;

import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ModuleLoader extends EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ModuleLoader.class);
    private final Vertx context;

    @EventContractor(values = {EventType.CREATE, EventType.INIT})
    private Single<JsonObject> installModule(JsonObject data) {
        String serviceId = data.getString("service_id");
        JsonObject deployCfg = data.getJsonObject("deploy_cfg");
        logger.info("Vertx install module {} with config {}...", serviceId, deployCfg);
        DeploymentOptions options = new DeploymentOptions().setConfig(deployCfg);
        return context.rxDeployVerticle(serviceId, options).doOnError(throwable -> {
            throw new EngineException(throwable);
        }).map(id -> new JsonObject().put("deploy_id", id));
    }

    @EventContractor(values = {EventType.REMOVE, EventType.HALT})
    private Single<JsonObject> removeModule(JsonObject data) {
        String deployId = data.getString("deploy_id");
        boolean isSilent = data.getBoolean("silent");
        logger.info("Vertx unload module {}...", deployId);
        return context.rxUndeploy(deployId).onErrorResumeNext(throwable -> {
            if (!isSilent) {
                throw new EngineException(throwable);
            }
            logger.warn("Module {} is gone in Vertx. Keep silent...", throwable, deployId);
            return CompletableObserver::onComplete;
        }).andThen(Single.just(new JsonObject().put("deploy_id", deployId)));
    }

    @EventContractor(values = EventType.UPDATE)
    private Single<JsonObject> reloadModule(JsonObject data) {
        String deployId = data.getString("deploy_id");
        JsonObject deployCfg = data.getJsonObject("deploy_cfg");
        logger.info("Vertx reload module {} with config {}...", deployId, deployCfg);
        return context.rxUndeploy(deployId).onErrorResumeNext(throwable -> {
            logger.debug("Module {} is gone in Vertx. Just installing...", throwable, deployId);
            return CompletableObserver::onComplete;
        }).andThen(installModule(data));
    }

    @Override
    protected List<EventType> getAvailableEvents() {
        return Arrays.asList(EventType.INIT, EventType.CREATE, EventType.UPDATE, EventType.HALT, EventType.REMOVE);
    }

}
