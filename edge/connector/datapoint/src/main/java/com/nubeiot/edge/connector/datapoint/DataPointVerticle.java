package com.nubeiot.edge.connector.datapoint;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.iotdata.model.DefaultCatalog;
import com.nubeiot.scheduler.QuartzSchedulerContext;
import com.nubeiot.scheduler.SchedulerProvider;

public final class DataPointVerticle extends ContainerVerticle {

    private InternalDataPointEntityHandler entityHandler;
    private MicroContext microCtx;
    private QuartzSchedulerContext schedulerCtx;

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        super.start();
        DataPointConfig pointCfg = IConfig.from(nubeConfig.getAppConfig(), DataPointConfig.class);
        this.addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .addProvider(new SchedulerProvider(), ctx -> schedulerCtx = (QuartzSchedulerContext) ctx);
        if (!pointCfg.isEnabledLowDb()) {
            this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, H2EntityHandler.class),
                             ctx -> entityHandler = ((SqlContext<H2EntityHandler>) ctx).getEntityHandler());
        }
        this.registerSuccessHandler(v -> successHandler(pointCfg, microCtx, schedulerCtx));
    }

    @Override
    public void registerEventbus(EventController controller) {
        super.registerEventbus(controller);
    }

    private void successHandler(DataPointConfig pointCfg, MicroContext microCtx, QuartzSchedulerContext schedulerCtx) {
        if (pointCfg.isEnabledLowDb()) {
            entityHandler = new LowDbEntityHandler(pointCfg);
        }
        entityHandler.setSchedulerRegisterModel(schedulerCtx.getRegisterModel());

        final ServiceDiscoveryController localController = microCtx.getLocalController();
        final EventMethodDefinition definition = EventMethodDefinition.create("/scheduler", "/:service_id",
                                                                              ActionMethodMapping.CUD_MAP, false);
        localController.addEventMessageRecord("datapoint-scheduler", schedulerCtx.getRegisterModel().getAddress(),
                                              definition).subscribe();
    }

    public DataPointEntityHandler getEntityHandler() {
        return this.entityHandler;
    }

}
