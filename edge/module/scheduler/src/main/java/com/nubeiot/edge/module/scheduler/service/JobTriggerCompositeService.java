package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.AbstractManyToOneEntityService;
import com.nubeiot.core.sql.service.ManyToOneReferenceEntityService.ManyToOneEntityTransformer;
import com.nubeiot.core.sql.validation.CompositeValidation;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.Metadata;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.Getter;
import lombok.NonNull;

abstract class JobTriggerCompositeService<P extends CompositePojo, M extends Metadata,
                                             V extends JobTriggerCompositeService>
    extends AbstractManyToOneEntityService<P, M, V>
    implements SchedulerService<P, M, V>, CompositeValidation<JobTrigger, JobTriggerComposite>,
               ManyToOneEntityTransformer {

    @Getter
    private QuartzSchedulerContext schedulerContext;

    JobTriggerCompositeService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
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

}
