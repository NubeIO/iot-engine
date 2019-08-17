package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.CompositeMetadata.AbstractCompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.service.AbstractManyToOneEntityService;
import com.nubeiot.core.sql.service.ManyToOneReferenceEntityService.ManyToOneEntityTransformer;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.sql.validation.CompositeValidation;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.scheduler.service.JobTriggerCompositeService.Metadata;
import com.nubeiot.iotdata.scheduler.model.Tables;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobTriggerDao;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.iotdata.scheduler.model.tables.records.JobTriggerRecord;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.Getter;
import lombok.NonNull;

abstract class JobTriggerCompositeService<M extends Metadata, V extends JobTriggerCompositeService>
    extends AbstractManyToOneEntityService<M, V> implements SchedulerService<M, V>,
                                                            CompositeValidation<JobTrigger, JobTriggerComposite>,
                                                            ManyToOneEntityTransformer {

    @Getter
    private QuartzSchedulerContext schedulerContext;

    JobTriggerCompositeService(@NonNull AbstractEntityHandler entityHandler,
                               @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    @Override
    public final Map<String, Function<String, ?>> jsonFieldConverter() {
        return Collections.singletonMap(secondaryParam(), Functions.toInt());
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return Collections.singleton(EventMethodDefinition.createDefault(servicePath(), "/:" + secondaryParam()));
    }

    abstract String secondaryParam();

    abstract String servicePath();

    static abstract class Metadata
        extends AbstractCompositeMetadata<Integer, JobTrigger, JobTriggerRecord, JobTriggerDao, JobTriggerComposite>
        implements SerialKeyEntity<JobTrigger, JobTriggerRecord, JobTriggerDao> {

        @Override
        @SuppressWarnings("unchecked")
        public final @NonNull Class<JobTriggerComposite> modelClass() { return JobTriggerComposite.class; }

        @Override
        public final @NonNull Class<JobTriggerDao> daoClass() { return JobTriggerDao.class; }

        @Override
        public final @NonNull JsonTable<JobTriggerRecord> table() { return Tables.JOB_TRIGGER; }

    }

}
