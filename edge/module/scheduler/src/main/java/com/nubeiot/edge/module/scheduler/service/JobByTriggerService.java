package com.nubeiot.edge.module.scheduler.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.ManyToOneReferenceEntityService;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobByTriggerMetadata;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class JobByTriggerService extends JobTriggerCompositeService<JobByTriggerMetadata, JobByTriggerService> {

    public JobByTriggerService(@NonNull AbstractEntityHandler entityHandler,
                               @NonNull QuartzSchedulerContext schedulerContext) {
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

    @SuppressWarnings("unchecked")
    @Override
    public JobEntityMetadata reference() {
        return JobEntityMetadata.INSTANCE;
    }

    @Override
    public JobByTriggerMetadata metadata() {
        return JobByTriggerMetadata.INSTANCE;
    }

    @Override
    public JobByTriggerService validation() {
        return this;
    }

    @Override
    public @NonNull ManyToOneReferenceEntityService.ManyToOneEntityTransformer transformer() {
        return this;
    }

    @Override
    public HasReferenceResource ref() {
        return this;
    }

}
