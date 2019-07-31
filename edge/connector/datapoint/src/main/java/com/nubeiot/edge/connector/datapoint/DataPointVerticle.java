package com.nubeiot.edge.connector.datapoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Single;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.connector.datapoint.service.DeviceService;
import com.nubeiot.edge.connector.datapoint.service.EquipmentService;
import com.nubeiot.edge.connector.datapoint.service.EventHttpService;
import com.nubeiot.edge.connector.datapoint.service.HistoryDataService;
import com.nubeiot.edge.connector.datapoint.service.HistorySettingService;
import com.nubeiot.edge.connector.datapoint.service.PointService;
import com.nubeiot.edge.connector.datapoint.service.RealtimeDataService;
import com.nubeiot.edge.connector.datapoint.service.RealtimeSettingService;
import com.nubeiot.edge.connector.datapoint.service.ScheduleSettingService;
import com.nubeiot.edge.connector.datapoint.service.TagPointService;
import com.nubeiot.edge.connector.datapoint.service.TransducerService;
import com.nubeiot.iotdata.model.DefaultCatalog;
import com.nubeiot.scheduler.QuartzSchedulerContext;
import com.nubeiot.scheduler.SchedulerProvider;

public final class DataPointVerticle extends ContainerVerticle {

    static final String LOWDB_MIGRATION = "LOWDB_MIGRATION";
    private final Set<EventHttpService> services = new HashSet<>();
    private DataPointEntityHandler entityHandler;
    private MicroContext microCtx;
    private QuartzSchedulerContext schedulerCtx;

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        super.start();
        DataPointConfig pointCfg = IConfig.from(nubeConfig.getAppConfig(), DataPointConfig.class);
        this.services.addAll(createServices(pointCfg));
        this.addSharedData(LOWDB_MIGRATION, pointCfg.getLowDbMigration());
        this.addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .addProvider(new SchedulerProvider(), ctx -> schedulerCtx = (QuartzSchedulerContext) ctx)
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, DataPointEntityHandler.class),
                         ctx -> entityHandler = ((SqlContext<DataPointEntityHandler>) ctx).getEntityHandler());
        this.registerSuccessHandler(v -> successHandler(services, microCtx, schedulerCtx));
    }

    @Override
    public void registerEventbus(EventController controller) {
        this.services.forEach(service -> controller.register(service.address(), service));
    }

    private List<EventHttpService> createServices(DataPointConfig pointCfg) {
        final HttpClientDelegate client = HttpClientDelegate.create(vertx.getDelegate(),
                                                                    pointCfg.getSyncServer().toHost());
        return Arrays.asList(new DeviceService(entityHandler, client), new EquipmentService(entityHandler, client),
                             new PointService(entityHandler, client), new HistoryDataService(entityHandler, client),
                             new HistorySettingService(entityHandler, client),
                             new RealtimeDataService(entityHandler, client),
                             new RealtimeSettingService(entityHandler, client),
                             new TransducerService(entityHandler, client),
                             new ScheduleSettingService(entityHandler, client),
                             new TagPointService(entityHandler, client));
    }

    private void successHandler(Set<EventHttpService> services, MicroContext microCtx,
                                QuartzSchedulerContext schedulerCtx) {
        final ServiceDiscoveryController discovery = microCtx.getLocalController();
        final List<Single<Record>> registers = new ArrayList<>();
        services.forEach(
            s -> s.definitions().forEach((k, v) -> registers.add(discovery.addEventMessageRecord(k, s.address(), v))));
        registers.add(
            discovery.addEventMessageRecord("datapoint-scheduler", schedulerCtx.getRegisterModel().getAddress(),
                                            EventMethodDefinition.create("/scheduler", "/:service_id",
                                                                         ActionMethodMapping.CUD_MAP, false)));
        Single.merge(registers).subscribe();
    }

}
