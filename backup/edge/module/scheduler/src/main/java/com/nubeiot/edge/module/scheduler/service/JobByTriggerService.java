package com.nubeiot.edge.module.scheduler.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class JobByTriggerService extends JobTriggerCompositeService {

    JobByTriggerService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
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

    @Override
    protected TriggerEntity getTrigger(JobTriggerComposite composite) {
        return composite.safeGetOther(reference().singularKeyName(), TriggerEntity.class);
    }

    @Override
    protected JobEntity getJob(JobTriggerComposite composite) {
        return composite.safeGetOther(resource().singularKeyName(), JobEntity.class);
    }

}
