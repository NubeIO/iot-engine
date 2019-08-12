package com.nubeiot.edge.module.scheduler;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.iotdata.scheduler.model.DefaultCatalog;
import com.nubeiot.scheduler.QuartzSchedulerContext;
import com.nubeiot.scheduler.SchedulerProvider;

public final class EdgeSchedulerVerticle extends ContainerVerticle {

    static final String SCHEDULER_ADDRESS = "SCHEDULER_ADDRESS";
    private QuartzSchedulerContext schedulerCtx;
    private SchedulerEntityHandler entityHandler;
    private MicroContext microCtx;

    @Override
    public void start() {
        super.start();
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, SchedulerEntityHandler.class),
                         ctx -> entityHandler = ((SqlContext<SchedulerEntityHandler>) ctx).getEntityHandler())
            .addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .addProvider(new SchedulerProvider(), ctx -> schedulerCtx = (QuartzSchedulerContext) ctx)
            .registerSuccessHandler(v -> successHandler());
    }

    private void successHandler() {
        this.addSharedData(SCHEDULER_ADDRESS, schedulerCtx.getRegisterModel().getAddress());
    }

}
