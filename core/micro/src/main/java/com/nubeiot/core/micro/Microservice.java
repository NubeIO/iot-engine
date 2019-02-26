package com.nubeiot.core.micro;

import io.reactivex.Completable;
import io.vertx.core.Future;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.component.UnitVerticle;

public final class Microservice extends UnitVerticle<MicroConfig, MicroContext> {

    Microservice() {
        super(new MicroContext());
    }

    @Override
    public void start() {
        super.start();
        logger.info("Setup micro-service...");
        getContext().create(Vertx.newInstance(vertx), config);
    }

    @Override
    public void stop(Future<Void> future) {
        Completable.merge(getContext().unregister()).doOnComplete(future::complete).doOnError(future::fail).subscribe();
    }

    @Override
    public Class<MicroConfig> configClass() {
        return MicroConfig.class;
    }

    @Override
    public String configFile() { return "micro.json"; }

}
