package com.nubeiot.scheduler;

import io.vertx.core.Future;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.utils.ExecutorHelpers;

public final class QuartzSchedulerUnit extends UnitVerticle<SchedulerConfig, QuartzSchedulerContext> {

    QuartzSchedulerUnit() {
        super(new QuartzSchedulerContext());
    }

    @Override
    public Class<SchedulerConfig> configClass() { return SchedulerConfig.class; }

    @Override
    public String configFile() { return "scheduler.json"; }

    @Override
    public void start(Future<Void> future) {
        this.start();
        ExecutorHelpers.blocking(vertx, () -> this.getContext().init(vertx, getSharedKey(), config))
                       .doOnSuccess(this::initRegisterListener)
                       .subscribe(success -> future.complete(), future::fail);
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        this.stop();
        ExecutorHelpers.blocking(vertx, () -> this.getContext().shutdown())
                       .subscribe(s -> future.complete(), future::fail);
    }

    private void initRegisterListener(QuartzSchedulerContext ctx) {
        SharedDataDelegate.getEventController(vertx, getSharedKey())
                          .register(ctx.getRegisterModel(), new RegisterScheduleListener(ctx.getScheduler(),
                                                                                         ctx.getRegisterModel()
                                                                                            .getEvents()));
    }

}
