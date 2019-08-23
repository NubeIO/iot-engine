package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobTriggerMetadata;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.Getter;
import lombok.NonNull;

abstract class JobTriggerCompositeService
    extends AbstractManyToManyEntityService<JobTriggerComposite, JobTriggerMetadata>
    implements SchedulerService<JobTriggerComposite, JobTriggerMetadata> {

    @Getter
    private QuartzSchedulerContext schedulerContext;

    JobTriggerCompositeService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    @Override
    public JobTriggerMetadata context() {
        return JobTriggerMetadata.INSTANCE;
    }

    @Override
    public final Map<String, Function<String, ?>> jsonFieldConverter() {
        final Map<String, Function<String, ?>> map = new HashMap<>();
        map.put(reference().requestKeyName(), Functions.toInt());
        map.put(resource().requestKeyName(), Functions.toInt());
        return Collections.unmodifiableMap(map);
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        final String servicePath = Urls.combinePath(
            Urls.capturePath(reference().singularKeyName(), reference().requestKeyName()),
            resource().singularKeyName());
        return Collections.singleton(EventMethodDefinition.createDefault(servicePath, resource().requestKeyName()));
    }

}
