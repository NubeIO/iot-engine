package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.TriggerEntityMetadata;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.tables.daos.TriggerEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.iotdata.scheduler.model.tables.records.TriggerEntityRecord;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public final class TriggerService extends
                                  AbstractSchedulerService<Integer, TriggerEntity, TriggerEntityRecord,
                                                              TriggerEntityDao, TriggerEntityMetadata> {

    public TriggerService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler, schedulerContext);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(
            EventMethodDefinition.createDefault("/trigger", "/:" + metadata().requestKeyName()));
    }

    @Override
    public TriggerEntity validateOnCreate(@NonNull TriggerEntity pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return TriggerConverter.validate(pojo);
    }

    @Override
    public TriggerEntity validateOnUpdate(@NonNull TriggerEntity dbData, @NonNull TriggerEntity pojo,
                                          @NonNull JsonObject headers) throws IllegalArgumentException {
        return super.validateOnUpdate(dbData, TriggerConverter.validate(pojo), headers);
    }

    @Override
    public TriggerEntity validateOnPatch(@NonNull TriggerEntity dbData, @NonNull TriggerEntity pojo,
                                         @NonNull JsonObject headers) throws IllegalArgumentException {
        return TriggerConverter.validate(super.validateOnPatch(dbData, pojo, headers));
    }

    @Override
    public TriggerEntityMetadata metadata() {
        return TriggerEntityMetadata.INSTANCE;
    }

}
