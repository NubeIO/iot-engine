package com.nubeiot.scheduler.job;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import io.vertx.core.Vertx;

import com.nubeiot.scheduler.SchedulerConfig;
import com.nubeiot.scheduler.job.VertxJob;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VertxJobFactory extends SimpleJobFactory implements JobFactory {

    private final Vertx vertx;
    private final String sharedKey;
    private final SchedulerConfig config;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Job job = super.newJob(bundle, scheduler);
        final Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
        if (jobClass.isAssignableFrom(VertxJob.class)) {
            return ((VertxJob) job).init(vertx, sharedKey, config);
        }
        return job;
    }

}
