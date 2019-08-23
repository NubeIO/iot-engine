package com.nubeiot.edge.module.scheduler.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class TriggerByJobService extends JobTriggerCompositeService {

    public TriggerByJobService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler, schedulerContext);
    }

    @Override
    public JobEntityMetadata reference() {
        return JobEntityMetadata.INSTANCE;
    }

    @Override
    public @NonNull TriggerEntityMetadata resource() {
        return TriggerEntityMetadata.INSTANCE;
    }

}
