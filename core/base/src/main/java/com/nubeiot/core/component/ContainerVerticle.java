package com.nubeiot.core.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeExceptionConverter;

import lombok.Getter;

/**
 * @see Container
 */
public abstract class ContainerVerticle extends AbstractVerticle implements Container {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<Class<? extends Unit>, UnitProvider<? extends Unit>> components = new LinkedHashMap<>();
    private final Map<Class<? extends Unit>, Consumer<? extends UnitContext>> afterSuccesses = new HashMap<>();
    private final Set<String> deployments = new HashSet<>();
    private final Map<String, Object> sharedData = new HashMap<>();
    private final String sharedKey = this.getClass().getName();
    @Getter
    protected EventController eventController;
    @Getter
    protected NubeConfig nubeConfig;

    @Override
    public void start() {
        this.nubeConfig = computeConfig(config());
        this.eventController = new EventController(vertx);
        this.registerEventbus(eventController);
        this.addSharedData(SharedDataDelegate.SHARED_EVENTBUS, this.eventController);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Future<Void> future) {
        this.start();
        this.vertx.sharedData().getLocalMap(sharedKey).getDelegate().putAll(sharedData);
        this.startUnits(future);
    }

    @Override
    public void stop(Future<Void> future) {
        this.stopUnits(future);
    }

    @Override
    public void registerEventbus(EventController controller) { }

    @Override
    public final Container addSharedData(String key, Object data) {
        this.sharedData.put(key, data);
        return this;
    }

    @Override
    public final <T extends Unit> Container addProvider(UnitProvider<T> provider) {
        this.components.put(provider.unitClass(), provider);
        return this;
    }

    @Override
    public final <C extends UnitContext, T extends Unit> Container addProvider(UnitProvider<T> provider,
                                                                               Consumer<C> successHandler) {
        this.addProvider(provider);
        this.afterSuccesses.put(provider.unitClass(), successHandler);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void startUnits(Future<Void> future) {
        final List<Single<String>> collect = components.entrySet().stream().map(entry -> {
            Unit unit = entry.getValue().get().registerSharedData(sharedKey);
            JsonObject deployConfig = IConfig.from(this.nubeConfig, unit.configClass()).toJson();
            DeploymentOptions options = new DeploymentOptions().setConfig(deployConfig);
            return vertx.rxDeployVerticle(unit, options)
                        .doOnSuccess(deployId -> succeed(unit, deployId))
                        .doOnError(t -> logger.error("Cannot start unit verticle {}", t, unit.getClass().getName()));
        }).collect(Collectors.toList());
        Single.zip(collect, objects -> objects.length).subscribe(count -> {
            logger.info("Deployed {} verticle(s)...", count);
            future.complete();
        }, throwable -> fail(future, throwable));
    }

    @Override
    public final void stopUnits(Future<Void> future) {
        Flowable.fromIterable(this.deployments)
                .parallel()
                .map(vertx::rxUndeploy)
                .reduce(Completable::mergeWith)
                .count()
                .subscribe(c -> {
                    logger.info("Uninstall {} verticle successfully", c);
                    future.complete();
                }, future::fail);
    }

    private void fail(Future<Void> future, Throwable throwable) {
        NubeException t = NubeExceptionConverter.from(throwable);
        logger.error("Cannot start container verticle {}", t, this.getClass().getName());
        future.fail(t);
    }

    @SuppressWarnings("unchecked")
    private void succeed(Unit unit, String deployId) {
        logger.info("Deployed Verticle '{}' successful with ID '{}'", unit.getClass().getName(), deployId);
        deployments.add(deployId);
        Consumer<UnitContext> consumer = (Consumer<UnitContext>) this.afterSuccesses.get(unit.getClass());
        if (Objects.nonNull(consumer)) {
            consumer.accept(unit.getContext().registerDeployId(deployId));
        }
    }

}
