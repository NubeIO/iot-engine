package com.nubeiot.edge.module.datapoint;

import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex;
import com.nubeiot.edge.module.datapoint.service.DataPointService;
import com.nubeiot.iotdata.edge.model.DefaultCatalog;

public final class DataPointVerticle extends ContainerVerticle {

    static final String LOWDB_MIGRATION = "LOWDB_MIGRATION";
    private DataPointEntityHandler entityHandler;
    private MicroContext microCtx;

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        super.start();
        final DataPointConfig pointCfg = IConfig.from(nubeConfig.getAppConfig(), DataPointConfig.class);
        this.addSharedData(LOWDB_MIGRATION, pointCfg.getLowdbMigration())
            .addSharedData(DataPointIndex.BUILTIN_DATA, pointCfg.getBuiltinData().toJson())
            .addSharedData(DataPointIndex.DATA_SYNC_CFG, pointCfg.getDataSyncConfig().toJson())
            .addSharedData(IDittoModel.CACHE_SYNC_CLASSES,
                           new ClassGraphCache<EntityMetadata>().register(IDittoModel::find))
            .addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, DataPointEntityHandler.class),
                         ctx -> entityHandler = ((SqlContext<DataPointEntityHandler>) ctx).getEntityHandler())
            .registerSuccessHandler(v -> successHandler());
    }

    private void successHandler() {
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
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(
                             e -> discovery.addEventMessageRecord(s.api(), s.address(), (EventMethodDefinition) e));
    }

}
