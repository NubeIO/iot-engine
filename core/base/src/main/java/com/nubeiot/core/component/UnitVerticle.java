package com.nubeiot.core.component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UnitVerticle<C extends IConfig, T extends UnitContext> extends AbstractVerticle
    implements Unit<C, T> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    private final T unitContext;
    protected C config;
    @Getter(value = AccessLevel.PROTECTED)
    private String sharedKey;

    @Override
    public void start() {
        logger.debug("Computing component configure from {} of {}", configFile(), configClass());
        this.config = computeConfig(config());
        logger.debug("Unit Configuration: {}", config.toJson().encode());
    }

    @Override
    public final T getContext() {
        return unitContext;
    }

    @Override
    public final Unit<C, T> registerSharedData(String sharedKey) {
        logger.debug("Register SharedData with shared key: {}", sharedKey);
        this.sharedKey = sharedKey;
        return this;
    }

    @Override
    public final <R> R getSharedData(String dataKey) {
        logger.debug("Retrieve SharedData by SharedKey {}", sharedKey);
        return SharedDataDelegate.getLocalDataValue(vertx, sharedKey, dataKey);
    }

}
