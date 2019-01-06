package com.nubeiot.core.component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeExceptionConverter;

import lombok.Getter;

public abstract class ContainerVerticle extends AbstractVerticle implements Container {

    private final Map<Class<? extends Unit>, UnitProvider<? extends Unit>> components = new LinkedHashMap<>();
    private final Map<Class<? extends Unit>, Consumer<? extends Unit>> afterSuccesses = new HashMap<>();
    private final Map<Class<? extends Unit>, String> deployments = new HashMap<>();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter
    protected NubeConfig nubeConfig;

    @Override
    public void start() {
        this.nubeConfig = IConfig.from(config(), NubeConfig.class);
    }

    @Override
    public void start(Future<Void> future) {
        this.start();
        this.startUnits(future);
    }

    @Override
    public void stop(Future<Void> future) {
        this.stopUnits(future);
    }

    @Override
    public <T extends Unit> Container addProvider(UnitProvider<T> provider) {
        this.components.put(provider.unitClass(), provider);
        return this;
    }

    @Override
    public <T extends Unit> Container addProvider(UnitProvider<T> provider, Consumer<T> successHandler) {
        this.addProvider(provider);
        this.afterSuccesses.put(provider.unitClass(), successHandler);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startUnits(Future<Void> future) {
        Flowable.fromIterable(components.entrySet()).map(entry -> {
            Unit unit = entry.getValue().get();
            JsonObject deployConfig = IConfig.from(this.nubeConfig, unit.configClass()).toJson();
            DeploymentOptions options = new DeploymentOptions().setConfig(deployConfig);
            return vertx.rxDeployVerticle(unit, options)
                        .subscribe(deployId -> succeed(unit, deployId), throwable -> fail(future, unit, throwable));
        }).count().subscribe(c -> {
            logger.info("Deployed {} verticle successfully", c);
            future.complete();
        }, future::fail);
    }

    @Override
    public void stopUnits(Future<Void> future) {
        Flowable.fromIterable(this.deployments.values())
                .parallel()
                .map(vertx::rxUndeploy)
                .reduce(Completable::mergeWith)
                .count()
                .subscribe(c -> {
                    logger.info("Uninstall {} verticle successfully", c);
                    future.complete();
                }, future::fail);
    }

    private void fail(Future<Void> future, Unit unit, Throwable throwable) {
        NubeException t = NubeExceptionConverter.from(throwable);
        if (t instanceof InitializerError) {
            future.fail(t);
            return;
        }
        logger.warn("Some issues when starting unit verticle {}", t, unit.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    private void succeed(Unit unit, String deployId) {
        logger.info("Deployed {} successful with {}", unit.getClass(), deployId);
        deployments.put(unit.getClass(), deployId);
        Consumer<Unit> consumer = (Consumer<Unit>) this.afterSuccesses.get(unit.getClass());
        if (Objects.nonNull(consumer)) {
            consumer.accept(unit);
        }
    }

}
