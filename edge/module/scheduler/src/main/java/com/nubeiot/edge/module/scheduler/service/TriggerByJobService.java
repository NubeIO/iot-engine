package com.nubeiot.edge.module.scheduler.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class TriggerByJobService extends JobTriggerCompositeService {

    TriggerByJobService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
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
    public Set<String> ignoreFields() {
        return Stream.of(super.ignoreFields(),
                         Collections.singletonList(resource().table().getJsonField(resource().table().THREAD)))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
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
