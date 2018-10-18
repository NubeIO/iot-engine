package com.nubeio.iot.edge.loader;

import java.util.function.Supplier;

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
public final class ModuleLoader {

    private static final Logger logger = LoggerFactory.getLogger(ModuleLoader.class);
    private final Supplier<Vertx> context;

    public Single<String> installModule(String moduleId, JsonObject deployConfig) {
        logger.info("Vertx install module {} with config {}...", moduleId, deployConfig);
        DeploymentOptions options = new DeploymentOptions().setConfig(deployConfig);
        return context.get().rxDeployVerticle(moduleId, options).doOnError(throwable -> {
            throw new EngineException(throwable);
        });
    }

    public Single<String> removeModule(String moduleId) {
        logger.info("Vertx unload module {}...", moduleId);
        return context.get().rxUndeploy(moduleId).doOnError(throwable -> {
            throw new EngineException(throwable);
        }).andThen(Single.just(moduleId));
    }

    public Single<String> reloadModule(String moduleId, JsonObject deployConfig) {
        logger.info("Vertx reload module {} with config {}...", moduleId, deployConfig);
        return context.get().rxUndeploy(moduleId).onErrorResumeNext(throwable -> {
            logger.debug("Module {} may not installed", throwable, moduleId);
            return CompletableObserver::onComplete;
        }).andThen(installModule(moduleId, deployConfig));
    }

}
