package com.nubeiot.edge.module.scheduler.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class JobByTriggerService extends JobTriggerCompositeService {

    public JobByTriggerService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler, schedulerContext);
    }

    @Override
    public TriggerEntityMetadata reference() {
        return TriggerEntityMetadata.INSTANCE;
    }

    @Override
    public @NonNull JobEntityMetadata resource() {
        return JobEntityMetadata.INSTANCE;
    }

}
