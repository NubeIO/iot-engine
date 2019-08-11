package com.nubeiot.scheduler.job;

import org.quartz.Job;
import org.quartz.JobDataMap;

import io.vertx.core.Vertx;

import com.nubeiot.scheduler.SchedulerConfig;

public interface VertxJob<J extends JobModel> extends Job {

    Vertx vertx();

    String sharedKey();

    SchedulerConfig config();

    Job init(Vertx vertx, String sharedKey, SchedulerConfig config);

    default J getJobModel(JobDataMap jobDataMap) {
        return (J) jobDataMap.get(JobModel.JOB_DATA_KEY);
    }

}
