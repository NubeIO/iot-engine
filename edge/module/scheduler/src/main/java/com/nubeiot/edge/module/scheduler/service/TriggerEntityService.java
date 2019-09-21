package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.Set;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.Getter;
import lombok.NonNull;

public final class TriggerEntityService extends AbstractEntityService<TriggerEntity, TriggerEntityMetadata>
    implements SchedulerService<TriggerEntity, TriggerEntityMetadata> {

    @Getter
    private QuartzSchedulerContext schedulerContext;

    public TriggerEntityService(@NonNull EntityHandler entityHandler,
                                @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    @Override
    public TriggerEntityMetadata context() {
        return TriggerEntityMetadata.INSTANCE;
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(
            EventMethodDefinition.createDefault(context().singularKeyName(), context().requestKeyName()));
    }

}
