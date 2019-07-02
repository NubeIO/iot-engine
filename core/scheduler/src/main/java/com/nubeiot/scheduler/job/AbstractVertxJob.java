package com.nubeiot.scheduler.job;

import org.quartz.Job;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.scheduler.SchedulerConfig;

public abstract class AbstractVertxJob<J extends JobModel> implements VertxJob<J> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Vertx vertx;
    protected String sharedKey;
    protected SchedulerConfig config;
    protected EventController controller;

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
        return this;
    }

}
