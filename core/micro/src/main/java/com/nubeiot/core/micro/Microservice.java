package com.nubeiot.core.micro;

import io.vertx.core.Future;

import com.nubeiot.core.component.UnitVerticle;

public final class Microservice extends UnitVerticle<MicroConfig, MicroContext> {

    Microservice() {
        super(new MicroContext());
    }

    @Override
    public void start() {
        super.start();
        logger.info("Setup micro-service...");
        getContext().setup(vertx, config, getSharedKey());
    }

    @Override
    public void stop(Future<Void> future) { getContext().unregister(future); }

    @Override
    public Class<MicroConfig> configClass() { return MicroConfig.class; }

    @Override
    public String configFile() { return "micro.json"; }

}
