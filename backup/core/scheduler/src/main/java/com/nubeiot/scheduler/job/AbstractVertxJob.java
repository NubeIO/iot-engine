package com.nubeiot.scheduler.job;

import java.util.Objects;
import java.util.function.Consumer;

import org.quartz.Job;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.scheduler.SchedulerConfig;

public abstract class AbstractVertxJob<J extends JobModel> implements VertxJob<J> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Vertx vertx;
    protected String sharedKey;
    protected SchedulerConfig config;
    protected EventbusClient controller;
    protected DeliveryEvent monitorEvent;

    @Override
    public Vertx vertx() { return vertx; }

    @Override
    public String sharedKey() { return sharedKey; }

    @Override
    public SchedulerConfig config() { return config; }

    @Override
    public Job init(Vertx vertx, String sharedKey, SchedulerConfig config) {
        this.vertx = vertx;
        this.sharedKey = sharedKey;
        this.config = config;
        this.controller = SharedDataDelegate.getEventController(vertx, sharedKey);
        this.monitorEvent = DeliveryEvent.builder()
                                         .address(config.getMonitorAddress())
                                         .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                         .action(EventAction.MONITOR)
                                         .build();
        return this;
    }

    protected Consumer<EventMessage> monitor(JobModel jobModel, DeliveryEvent callbackEvent) {
        DeliveryEvent event = Objects.isNull(callbackEvent) ? monitorEvent : callbackEvent;
        return msg -> {
            if (!jobModel.forwardIfFailure() && msg.isError()) {
                return;
            }
            logger.info("Forward JOB result to '{}'", event.getAddress());
            if (logger.isTraceEnabled()) {
                logger.trace("JOB Result: {}", msg.toJson());
            }
            controller.fire(event.getAddress(), event.getPattern(), EventMessage.override(msg, event.getAction()));
        };
    }

    protected Consumer<ErrorMessage> errorMonitor(JobModel jobModel) {
        return jobModel.forwardIfFailure()
               ? null
               : msg -> controller.fire(monitorEvent.getAddress(), monitorEvent.getPattern(),
                                        EventMessage.error(monitorEvent.getAction(), null, msg));
    }

}
