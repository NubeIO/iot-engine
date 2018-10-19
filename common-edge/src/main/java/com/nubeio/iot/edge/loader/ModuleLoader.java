package com.nubeio.iot.edge.loader;

import java.util.function.Supplier;

import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.event.IEventHandler;
import com.nubeio.iot.share.event.RequestData;
import com.nubeio.iot.share.exceptions.EngineException;
import com.nubeio.iot.share.exceptions.NubeException;

import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ModuleLoader implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ModuleLoader.class);
    private final Supplier<Vertx> context;

    public Single<JsonObject> handle(EventType action, RequestData requestData) {
        JsonObject body = requestData.getBody();
        if (EventType.CREATE == action || EventType.INIT == action) {
            return installModule(body);
        }
        if (EventType.UPDATE == action) {
            return reloadModule(body);
        }
        if (EventType.REMOVE == action || EventType.HALT == action) {
            return removeModule(body);
        }
        throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT,
                                "Unsupported action " + action + " when interact physical module");
    }

    private Single<JsonObject> installModule(JsonObject data) {
        String serviceId = data.getString("service_id");
        JsonObject deployCfg = data.getJsonObject("deploy_cfg");
        logger.info("Vertx install module {} with config {}...", serviceId, deployCfg);
        DeploymentOptions options = new DeploymentOptions().setConfig(deployCfg);
        return context.get().rxDeployVerticle(serviceId, options).doOnError(throwable -> {
            throw new EngineException(throwable);
        }).map(id -> new JsonObject().put("deploy_id", id));
    }

    private Single<JsonObject> removeModule(JsonObject data) {
        String deployId = data.getString("deploy_id");
        boolean isSilent = data.getBoolean("silent");
        logger.info("Vertx unload module {}...", deployId);
        return context.get().rxUndeploy(deployId).onErrorResumeNext(throwable -> {
            if (!isSilent) {
                throw new EngineException(throwable);
            }
            logger.warn("Module {} is gone in Vertx. Keep silent...", throwable, deployId);
            return CompletableObserver::onComplete;
        }).andThen(Single.just(new JsonObject().put("deploy_id", deployId)));
    }

    private Single<JsonObject> reloadModule(JsonObject data) {
        String deployId = data.getString("deploy_id");
        JsonObject deployCfg = data.getJsonObject("deploy_cfg");
        logger.info("Vertx reload module {} with config {}...", deployId, deployCfg);
        return context.get().rxUndeploy(deployId).onErrorResumeNext(throwable -> {
            logger.debug("Module {} is gone in Vertx. Just installing...", throwable, deployId);
            return CompletableObserver::onComplete;
        }).andThen(installModule(data));
    }

}
