package com.nubeiot.edge.module.scheduler.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
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

    TriggerEntityService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    @Override
    public TriggerEntityMetadata context() {
        return TriggerEntityMetadata.INSTANCE;
    }

    @Override
    public Set<String> ignoreFields(@NonNull RequestData requestData) {
        return Stream.of(super.ignoreFields(requestData),
                         Collections.singletonList(context().table().getJsonField(context().table().THREAD)))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

}
