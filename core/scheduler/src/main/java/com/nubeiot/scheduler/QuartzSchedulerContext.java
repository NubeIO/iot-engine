package com.nubeiot.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.scheduler.job.VertxJobFactory;

import lombok.Getter;

@Getter
public final class QuartzSchedulerContext extends UnitContext {

    private Scheduler scheduler;
    private EventModel registerModel;

    QuartzSchedulerContext init(Vertx vertx, String sharedKey, SchedulerConfig config) {
        this.registerModel = EventModel.builder()
                                       .address(config.getRegisterAddress())
                                       .local(vertx.isClustered())
                                       .pattern(EventPattern.REQUEST_RESPONSE)
                                       .addEvents(EventAction.CREATE, EventAction.REMOVE)
                                       .build();
        try {
            final DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
            factory.createScheduler(config.getSchedulerName(), config.getSchedulerName(),
                                    new QuartzVertxThreadPool(vertx, sharedKey, config.getMonitorAddress(),
                                                              config.getWorkerConfig()), new RAMJobStore());
            scheduler = factory.getScheduler(config.getSchedulerName());
            scheduler.setJobFactory(new VertxJobFactory(vertx, sharedKey, config));
            scheduler.start();
            return this;
        } catch (SchedulerException e) {
            throw new InitializerError("Cannot start Quartz Scheduler", e);
        }
    }

    void shutdown() {
        try {
            scheduler.shutdown();
            scheduler = null;
        } catch (SchedulerException e) {
            logger.warn("Cannot shutdown Quartz Scheduler");
        }
    }

}
