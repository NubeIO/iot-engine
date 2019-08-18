package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.Getter;
import lombok.NonNull;

public final class TriggerEntityService
    extends AbstractEntityService<TriggerEntity, TriggerEntityMetadata, TriggerEntityService>
    implements SchedulerService<TriggerEntity, TriggerEntityMetadata, TriggerEntityService>,
               EntityValidation<TriggerEntity>, EntityTransformer {

    @Getter
    private QuartzSchedulerContext schedulerContext;

    public TriggerEntityService(@NonNull EntityHandler entityHandler,
                                @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    @Override
    public TriggerEntity onCreate(@NonNull TriggerEntity pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return TriggerConverter.validate(pojo);
    }

    @Override
    public TriggerEntity onUpdate(@NonNull TriggerEntity dbData, @NonNull TriggerEntity pojo,
                                  @NonNull JsonObject headers) throws IllegalArgumentException {
        return EntityValidation.super.onUpdate(dbData, TriggerConverter.validate(pojo), headers);
    }

    @Override
    public TriggerEntity onPatch(@NonNull TriggerEntity dbData, @NonNull TriggerEntity pojo,
                                 @NonNull JsonObject headers) throws IllegalArgumentException {
        return TriggerConverter.validate(EntityValidation.super.onPatch(dbData, pojo, headers));
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(
            EventMethodDefinition.createDefault("/trigger", "/:" + metadata().requestKeyName()));
    }

    @Override
    public TriggerEntityMetadata metadata() {
        return TriggerEntityMetadata.INSTANCE;
    }

    @Override
    public TriggerEntityService validation() {
        return this;
    }

    @Override
    public @NonNull EntityTransformer transformer() {
        return this;
    }

}
