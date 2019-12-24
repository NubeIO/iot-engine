package com.nubeiot.edge.module.scheduler;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.register.EventHttpServiceRegister;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.module.scheduler.service.SchedulerService;
import com.nubeiot.iotdata.scheduler.model.DefaultCatalog;
import com.nubeiot.scheduler.QuartzSchedulerContext;
import com.nubeiot.scheduler.SchedulerProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class EdgeSchedulerVerticle extends ContainerVerticle {

    private final Class<? extends SchedulerEntityHandler> entityHandlerClass;
    private QuartzSchedulerContext schedulerCtx;
    private SchedulerEntityHandler entityHandler;
    private MicroContext microCtx;

    EdgeSchedulerVerticle() {
        this.entityHandlerClass = SchedulerEntityHandler.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        super.start();
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass),
                         ctx -> entityHandler = ((SqlContext<SchedulerEntityHandler>) ctx).getEntityHandler())
            .addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .addProvider(new SchedulerProvider(), ctx -> schedulerCtx = (QuartzSchedulerContext) ctx)
            .registerSuccessHandler(v -> successHandler());
    }

    private void successHandler() {
        EventHttpServiceRegister.create(vertx.getDelegate(), getSharedKey(),
                                        () -> SchedulerService.createServices(entityHandler, schedulerCtx))
                                .publish(microCtx.getLocalController())
                                .flatMapObservable(ignore -> entityHandler.register(schedulerCtx))
                                .toList()
                                .doOnSuccess(r -> logger.info("Registered {} schedulers", r.size()))
                                .subscribe();
    }

}
