package com.nubeiot.core.micro;

import io.vertx.core.Future;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.event.EventController;

public final class Microservice extends UnitVerticle<MicroConfig, MicroContext> {

    Microservice() {
        super(new MicroContext());
    }

    @Override
    public void start() {
        super.start();
        logger.info("Setup micro-service...");
        getContext().create(vertx, config, getSharedKey());
        final EventController eventClient = getSharedData(SharedDataDelegate.SHARED_EVENTBUS);
        eventClient.register(config.getGatewayConfig().getIndexAddress(), new ServiceGatewayIndex(getContext()));
    }

    @Override
    public void stop(Future<Void> future) { getContext().unregister(future); }

    @Override
    public Class<MicroConfig> configClass() { return MicroConfig.class; }

    @Override
    public String configFile() { return "micro.json"; }

}
