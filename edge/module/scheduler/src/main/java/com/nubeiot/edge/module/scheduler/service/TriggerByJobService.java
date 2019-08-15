package com.nubeiot.edge.module.scheduler.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerByJobMetadata;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class TriggerByJobService extends JobTriggerCompositeService<TriggerByJobMetadata> {

    public TriggerByJobService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler, schedulerContext);
    }

    @Override
    String secondaryParam() {
        return reference().requestKeyName();
    }

    String servicePath() {
        return "/job/:" + metadata().requestKeyName() + "/trigger";
    }

    @Override
    @SuppressWarnings("unchecked")
    public TriggerEntityMetadata reference() {
        return TriggerEntityMetadata.INSTANCE;
    }

    @Override
    public TriggerByJobMetadata metadata() {
        return TriggerByJobMetadata.INSTANCE;
    }

}
