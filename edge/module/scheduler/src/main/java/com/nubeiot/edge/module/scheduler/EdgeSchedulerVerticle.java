package com.nubeiot.edge.module.scheduler;

import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
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

    public EdgeSchedulerVerticle() {
        this.entityHandlerClass = SchedulerEntityHandler.class;
    }

    @Override
    public void start() {
        super.start();
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass),
                         ctx -> entityHandler = ((SqlContext<SchedulerEntityHandler>) ctx).getEntityHandler())
            .addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .addProvider(new SchedulerProvider(), ctx -> schedulerCtx = (QuartzSchedulerContext) ctx)
            .registerSuccessHandler(v -> successHandler());
    }

    private void successHandler() {
        EventController controller = SharedDataDelegate.getEventController(vertx.getDelegate(), getSharedKey());
        ServiceDiscoveryController discovery = microCtx.getLocalController();
        Observable.fromIterable(SchedulerService.createServices(entityHandler, schedulerCtx))
                  .doOnEach(s -> Optional.ofNullable(s.getValue())
                                         .ifPresent(service -> controller.register(service.address(), service)))
                  .filter(s -> Objects.nonNull(s.definitions()))
                  .flatMap(s -> Observable.fromIterable(s.definitions())
                                          .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e)))
                  .subscribe();
    }

}
