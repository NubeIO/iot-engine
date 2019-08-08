package com.nubeiot.edge.connector.datapoint;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.connector.datapoint.service.DataPointService;
import com.nubeiot.iotdata.model.DefaultCatalog;
import com.nubeiot.scheduler.QuartzSchedulerContext;
import com.nubeiot.scheduler.SchedulerProvider;

public final class DataPointVerticle extends ContainerVerticle {

    static final String LOWDB_MIGRATION = "LOWDB_MIGRATION";
    static final String SCHEDULER_ADDRESS = "SCHEDULER_ADDRESS";
    private DataPointEntityHandler entityHandler;
    private MicroContext microCtx;
    private QuartzSchedulerContext schedulerCtx;

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        super.start();
        DataPointConfig pointCfg = IConfig.from(nubeConfig.getAppConfig(), DataPointConfig.class);
        this.addSharedData(LOWDB_MIGRATION, pointCfg.getLowdbMigration())
            .addSharedData(DataPointEntityHandler.BUILTIN_DATA, pointCfg.getBuiltinData().toJson())
            .addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .addProvider(new SchedulerProvider(), ctx -> schedulerCtx = (QuartzSchedulerContext) ctx)
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, DataPointEntityHandler.class),
                         ctx -> entityHandler = ((SqlContext<DataPointEntityHandler>) ctx).getEntityHandler())
            .registerSuccessHandler(v -> successHandler());
    }

    private void successHandler() {
        this.addSharedData(SCHEDULER_ADDRESS, schedulerCtx.getRegisterModel().getAddress());
        EventController controller = SharedDataDelegate.getEventController(vertx.getDelegate(), getSharedKey());
        ServiceDiscoveryController discovery = microCtx.getLocalController();
        Observable.fromIterable(DataPointService.createServices(entityHandler))
                  .doOnEach(s -> Optional.ofNullable(s.getValue())
                                         .ifPresent(service -> controller.register(service.address(), service)))
                  .filter(s -> Objects.nonNull(s.definitions()))
                  .flatMap(s -> registerEndpoint(discovery, s))
                  .subscribe();
    }

    @SuppressWarnings("unchecked")
    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, DataPointService s) {
        return Observable.fromIterable(((Map<String, EventMethodDefinition>) s.definitions()).entrySet())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(e.getKey(), s.address(), e.getValue()));
    }

}
