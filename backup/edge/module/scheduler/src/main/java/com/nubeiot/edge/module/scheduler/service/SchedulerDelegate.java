package com.nubeiot.edge.module.scheduler.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero88.utils.Reflections.ReflectionClass;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.AbstractEntityServiceDelegate;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

final class SchedulerDelegate<P extends VertxPojo, M extends EntityMetadata>
    extends AbstractEntityServiceDelegate<P, M, SchedulerService<P, M>> implements SchedulerService<P, M> {

    SchedulerDelegate(@NonNull EntityHandler entityHandler, @NonNull Class<SchedulerService<P, M>> serviceClass,
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
    public QuartzSchedulerContext getSchedulerContext() {
        return unwrap().getSchedulerContext();
    }

    @Override
    public String api() {
        return unwrap().api();
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return unwrap().definitions();
    }

}
