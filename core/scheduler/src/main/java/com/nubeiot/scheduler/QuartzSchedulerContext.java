package com.nubeiot.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.scheduler.job.VertxJobFactory;

import lombok.Getter;

public class QuartzSchedulerContext extends UnitContext {

    @Getter
    private Scheduler scheduler;

    QuartzSchedulerContext init(Vertx vertx, String sharedKey, SchedulerConfig config) {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
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
