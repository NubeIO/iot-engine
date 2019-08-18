package com.nubeiot.edge.module.scheduler.service;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

public interface SchedulerService<P extends VertxPojo, M extends EntityMetadata, V extends EntityValidation>
    extends EntityService<P, M, V>, EventHttpService {

    @SuppressWarnings("unchecked")
    static Set<? extends SchedulerService> createServices(@NonNull EntityHandler entityHandler,
                                                          @NonNull QuartzSchedulerContext schedulerCtx) {
        return ReflectionClass.stream(SchedulerService.class.getPackage().getName(), SchedulerService.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> new SchedulerDelegate(entityHandler, clazz, schedulerCtx))
                              .collect(Collectors.toSet());
    }

    QuartzSchedulerContext getSchedulerContext();

    @Override
    default String api() {
        return "scheduler." + this.getClass().getSimpleName();
    }

}
