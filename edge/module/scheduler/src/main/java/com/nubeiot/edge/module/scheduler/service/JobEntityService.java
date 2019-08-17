package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.Getter;
import lombok.NonNull;

public final class JobEntityService extends AbstractEntityService<JobEntityMetadata, JobEntityService>
    implements SchedulerService<JobEntityMetadata, JobEntityService>, EntityValidation<JobEntity>, EntityTransformer {

    @Getter
    private QuartzSchedulerContext schedulerContext;

    public JobEntityService(@NonNull AbstractEntityHandler entityHandler,
                            @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(EventMethodDefinition.createDefault("/job", "/:" + metadata().requestKeyName()));
    }

    @Override
    public JobEntityMetadata metadata() {
        return JobEntityMetadata.INSTANCE;
    }

    @Override
    public JobEntityService validation() {
        return this;
    }

    @Override
    public @NonNull EntityTransformer transformer() {
        return this;
    }

    @Override
    public JobEntity onCreate(@NonNull JobEntity pojo, @NonNull JsonObject headers) throws IllegalArgumentException {
        return JobConverter.validate(pojo);
    }

    @Override
    public JobEntity onUpdate(@NonNull JobEntity dbData, @NonNull JobEntity pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return EntityValidation.super.onUpdate(dbData, JobConverter.validate(pojo), headers);
    }

    @Override
    public JobEntity onPatch(@NonNull JobEntity dbData, @NonNull JobEntity pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return JobConverter.validate(EntityValidation.super.onPatch(dbData, pojo, headers));
    }

}
