package com.nubeiot.scheduler;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.scheduler.solution.SchedulerConfig;

public class QuartzSchedulerUnit extends UnitVerticle<SchedulerConfig, QuartzSchedulerContext> {

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
        this.getContext().init(vertx, config);
        EventController controller = SharedDataDelegate.getLocalDataValue(vertx, getSharedKey(),
                                                                          SharedDataDelegate.SHARED_EVENTBUS);
        controller.register(this.getContext().getRegisterEventModel(), new RegisterScheduleListener(this.getContext()));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        this.getContext().shutdown();
    }

}
