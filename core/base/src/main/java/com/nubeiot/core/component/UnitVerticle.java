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
    }

    @Override
    public final Unit<C> registerSharedData(String sharedKey) {
        this.sharedKey = sharedKey;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T getSharedData(String key) {
        return (T) this.vertx.sharedData().getLocalMap(sharedKey).get(key);
    }

}
