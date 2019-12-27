package com.nubeiot.edge.module.scheduler.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.Getter;
import lombok.NonNull;

public final class JobEntityService extends AbstractEntityService<JobEntity, JobEntityMetadata>
    implements SchedulerService<JobEntity, JobEntityMetadata> {

    @Getter
    private QuartzSchedulerContext schedulerContext;

    JobEntityService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    @Override
    public JobEntityMetadata context() {
        return JobEntityMetadata.INSTANCE;
    }

}
