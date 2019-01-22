package com.nubeiot.core.component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;

public abstract class UnitVerticle<C extends IConfig> extends AbstractVerticle implements Unit<C> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected C config;
    private String sharedKey;

    @Override
    public void start() {
        logger.debug("Computing component configure from {} of {}", configFile(), configClass());
        this.config = computeConfig(config());
        logger.debug("Unit Configuration: {}", config.toJson().encode());
    }

    @Override
    public final Unit<C> registerSharedData(String sharedKey) {
        logger.debug("Register SharedData with shared key: {}", sharedKey);
        this.sharedKey = sharedKey;
        return this;
    }

    @Override
    public final <T> T getSharedData(String dataKey) {
        logger.debug("Retrieve SharedData by SharedKey {}", sharedKey);
        return SharedDataDelegate.getSharedDataValue(k -> vertx.sharedData().getLocalMap(sharedKey).get(k), dataKey);
    }

}
