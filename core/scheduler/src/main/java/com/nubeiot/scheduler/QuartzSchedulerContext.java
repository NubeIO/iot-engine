package com.nubeiot.scheduler;

import java.util.Arrays;
import java.util.List;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.scheduler.solution.SchedulerConfig;

import lombok.Getter;

public class QuartzSchedulerContext extends UnitContext {

    static final List<EventAction> REGISTER_ACTION = Arrays.asList(EventAction.CREATE, EventAction.REMOVE);

    @Getter
    private Scheduler scheduler;
    @Getter
    private EventModel registerEventModel;

    QuartzSchedulerContext init(Vertx vertx, SchedulerConfig config) {
        registerEventModel = EventModel.builder()
                                       .address(config.getAddress())
                                       .local(vertx.isClustered())
                                       .pattern(EventPattern.REQUEST_RESPONSE)
                                       .events(REGISTER_ACTION)
                                       .build();
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
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
