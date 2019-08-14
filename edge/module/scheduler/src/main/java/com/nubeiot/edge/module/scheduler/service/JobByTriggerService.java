package com.nubeiot.edge.module.scheduler.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobByTriggerMetadata;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class JobByTriggerService extends JobTriggerSchedulerService<JobByTriggerMetadata> {

    public JobByTriggerService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler, schedulerContext);
    }

    @Override
    String secondaryParam() {
        return reference().requestKeyName();
    }

    @Override
    String servicePath() {
        return "/trigger/:" + metadata().requestKeyName() + "/job";
    }

    @Override
    public EntityMetadata reference() {
        return SchedulerMetadata.JobEntityMetadata.INSTANCE;
    }

    @Override
    public JobByTriggerMetadata metadata() {
        return JobByTriggerMetadata.INSTANCE;
    }

}
