package com.nubeiot.scheduler;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

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
        this.initRegisterListener(this.getContext().init(vertx, getSharedKey(), config));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        this.getContext().shutdown();
    }

    private void initRegisterListener(QuartzSchedulerContext context) {
        EventModel model = EventModel.builder()
                                     .address(config.getAddress())
                                     .local(vertx.isClustered())
                                     .pattern(EventPattern.REQUEST_RESPONSE)
                                     .addEvents(EventAction.CREATE, EventAction.REMOVE)
                                     .build();
        SharedDataDelegate.getEventController(vertx, getSharedKey())
                          .register(model, new RegisterScheduleListener(context.getScheduler(), model.getEvents()));
    }

}
