package com.nubeiot.scheduler;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.component.UnitVerticle;

public final class QuartzSchedulerUnit extends UnitVerticle<SchedulerConfig, QuartzSchedulerContext> {

    QuartzSchedulerUnit() {
        super(new QuartzSchedulerContext());
    }

    @Override
    public Class<SchedulerConfig> configClass() { return SchedulerConfig.class; }

    @Override
    public String configFile() { return "scheduler.json"; }

    @Override
    public void start() {
        super.start();
        this.initRegisterListener(this.getContext().init(vertx, getSharedKey(), config));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        this.getContext().shutdown();
    }

    private void initRegisterListener(QuartzSchedulerContext ctx) {
        SharedDataDelegate.getEventController(vertx, getSharedKey())
                          .register(ctx.getRegisterModel(), new RegisterScheduleListener(ctx.getScheduler(),
                                                                                         ctx.getRegisterModel()
                                                                                            .getEvents()));
    }

}
