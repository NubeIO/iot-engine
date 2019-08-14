package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobEntityMetadata;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.records.JobEntityRecord;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class JobService
    extends AbstractSchedulerService<Integer, JobEntity, JobEntityRecord, JobEntityDao, JobEntityMetadata> {

    public JobService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler, schedulerContext);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(EventMethodDefinition.createDefault("/job", "/:" + metadata().requestKeyName()));
    }

    @Override
    public JobEntity validateOnCreate(@NonNull JobEntity pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return JobConverter.validate(pojo);
    }

    @Override
    public JobEntity validateOnUpdate(@NonNull JobEntity dbData, @NonNull JobEntity pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return super.validateOnUpdate(dbData, JobConverter.validate(pojo), headers);
    }

    @Override
    public JobEntity validateOnPatch(@NonNull JobEntity dbData, @NonNull JobEntity pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return JobConverter.validate(super.validateOnPatch(dbData, pojo, headers));
    }

    @Override
    public JobEntityMetadata metadata() {
        return JobEntityMetadata.INSTANCE;
    }

}
