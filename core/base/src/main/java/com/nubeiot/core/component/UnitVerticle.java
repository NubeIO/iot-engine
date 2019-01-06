package com.nubeiot.core.component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;

public abstract class UnitVerticle<C extends IConfig> extends AbstractVerticle implements Unit<C> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected C config;

    @Override
    public void start() {
        logger.debug("Computing component configure from {} of {}", configFile(), configClass());
        this.config = computeConfig(config());
    }

}
