package com.nubeiot.edge.module.datapoint;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.register.EventHttpServiceRegister;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.module.datapoint.service.DataPointService;

import lombok.NonNull;

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
            .addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .addProvider(new SqlProvider<>(DataPointEntityHandler.class),
                         ctx -> entityHandler = ((SqlContext<DataPointEntityHandler>) ctx).getEntityHandler())
            .registerSuccessHandler(v -> successHandler());
    }

    private void successHandler() {
        final @NonNull Supplier<Set<DataPointService>> supplier = () -> DataPointService.createServices(entityHandler);
        EventHttpServiceRegister.<DataPointService>builder().vertx(vertx)
                                                            .sharedKey(getSharedKey())
                                                            .eventServices(supplier)
                                                            .afterRegisterEventbusAddress(
                                                                s -> entityHandler.addServiceCache(s))
                                                            .build()
                                                            .publish(microCtx.getLocalController())
                                                            .subscribe();
    }

}
