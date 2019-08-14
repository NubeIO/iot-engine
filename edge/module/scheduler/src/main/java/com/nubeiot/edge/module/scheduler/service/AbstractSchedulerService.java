package com.nubeiot.edge.module.scheduler.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.sql.AbstractEntityService;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class AbstractSchedulerService<KEY, POJO extends VertxPojo, RECORD extends UpdatableRecord<RECORD>,
                                                  DAO extends VertxDAO<RECORD, POJO, KEY>,
                                                  METADATA extends EntityMetadata<KEY, POJO, RECORD, DAO>>
    extends AbstractEntityService<KEY, POJO, RECORD, DAO, METADATA> implements EventHttpService {

    private final QuartzSchedulerContext schedulerContext;

    AbstractSchedulerService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    public static Set<? extends AbstractSchedulerService> createServices(@NonNull EntityHandler entityHandler,
                                                                         @NonNull QuartzSchedulerContext schedulerCtx) {
        final Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(EntityHandler.class, entityHandler);
        inputs.put(QuartzSchedulerContext.class, schedulerCtx);
        return ReflectionClass.stream(AbstractSchedulerService.class.getPackage().getName(),
                                      AbstractSchedulerService.class, ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    @Override
    public String api() {
        return "scheduler." + this.getClass().getSimpleName();
    }

    @Override
    public boolean enableTimeAudit() {
        return true;
    }

}
