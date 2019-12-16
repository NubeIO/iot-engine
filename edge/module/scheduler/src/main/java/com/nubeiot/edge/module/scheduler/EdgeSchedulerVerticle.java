package com.nubeiot.edge.module.scheduler;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
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
        final ServiceDiscoveryController discover = microCtx.getLocalController();
        final Supplier<Set<SchedulerService>> supplier = () -> SchedulerService.createServices(entityHandler,
                                                                                               schedulerCtx);
        final EventHttpServiceRegister<SchedulerService> register = new EventHttpServiceRegister<>(vertx.getDelegate(),
                                                                                                   getSharedKey(),
                                                                                                   supplier);
        register.publish(discover)
                .toList()
                .doOnSuccess(r -> logger.info("Publish {} APIs", r.size()))
                .flatMapObservable(ignore -> entityHandler.register(schedulerCtx))
                .toList()
                .doOnSuccess(r -> logger.info("Register {} schedulers", r.size()))
                .subscribe();
    }

}
