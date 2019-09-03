package com.nubeiot.edge.module.scheduler.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
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

    @Override
    protected TriggerEntity getTrigger(JobTriggerComposite composite) {
        return composite.safeGetOther(resource().singularKeyName(), TriggerEntity.class);
    }

    @Override
    protected JobEntity getJob(JobTriggerComposite composite) {
        return composite.safeGetOther(reference().singularKeyName(), JobEntity.class);
    }
}
