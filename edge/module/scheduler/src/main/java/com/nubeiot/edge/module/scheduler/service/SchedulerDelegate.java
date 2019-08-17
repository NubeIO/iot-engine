package com.nubeiot.edge.module.scheduler.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.EntityServiceDelegate;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

class SchedulerDelegate<M extends EntityMetadata, V extends EntityValidation>
    extends EntityServiceDelegate<M, V, SchedulerService<M, V>> implements SchedulerService<M, V> {

    SchedulerDelegate(@NonNull AbstractEntityHandler entityHandler, @NonNull Class<SchedulerService<M, V>> serviceClass,
                      @NonNull QuartzSchedulerContext schedulerContext) {
        super(ReflectionClass.createObject(serviceClass, createInputs(entityHandler, schedulerContext)));
    }

    @NonNull
    private static Map<Class, Object> createInputs(@NonNull AbstractEntityHandler entityHandler,
                                                   @NonNull QuartzSchedulerContext schedulerContext) {
        Map<Class, Object> params = new LinkedHashMap<>();
        params.put(AbstractEntityHandler.class, entityHandler);
        params.put(QuartzSchedulerContext.class, schedulerContext);
        return params;
    }

    @Override
    public String address() {
        return unwrap().address();
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return unwrap().definitions();
    }

    @Override
    public QuartzSchedulerContext getSchedulerContext() {
        return unwrap().getSchedulerContext();
    }

    @Override
    public String api() {
        return unwrap().api();
    }

}
