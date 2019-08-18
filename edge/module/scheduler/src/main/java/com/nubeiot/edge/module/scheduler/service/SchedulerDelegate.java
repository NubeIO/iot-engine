package com.nubeiot.edge.module.scheduler.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.EntityServiceDelegate;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

final class SchedulerDelegate<P extends VertxPojo, M extends EntityMetadata, V extends EntityValidation>
    extends EntityServiceDelegate<P, M, V, SchedulerService<P, M, V>> implements SchedulerService<P, M, V> {

    SchedulerDelegate(@NonNull EntityHandler entityHandler, @NonNull Class<SchedulerService<P, M, V>> serviceClass,
                      @NonNull QuartzSchedulerContext schedulerContext) {
        super(ReflectionClass.createObject(serviceClass, createInputs(entityHandler, schedulerContext)));
    }

    @NonNull
    private static Map<Class, Object> createInputs(@NonNull EntityHandler entityHandler,
                                                   @NonNull QuartzSchedulerContext schedulerContext) {
        Map<Class, Object> params = new LinkedHashMap<>();
        params.put(EntityHandler.class, entityHandler);
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
