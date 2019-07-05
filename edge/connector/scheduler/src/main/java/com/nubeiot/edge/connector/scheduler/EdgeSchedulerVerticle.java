package com.nubeiot.edge.connector.scheduler;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.scheduler.QuartzSchedulerContext;
import com.nubeiot.scheduler.SchedulerProvider;

public final class EdgeSchedulerVerticle extends ContainerVerticle {

    private QuartzSchedulerContext schedulerContext;
    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        addProvider(new SchedulerProvider(), ctx -> schedulerContext = (QuartzSchedulerContext) ctx);
        addProvider(new MicroserviceProvider(), ctx -> microContext = (MicroContext) ctx);
        registerSuccessHandler(v -> this.publishService(microContext, schedulerContext));
    }

    private void publishService(MicroContext microCtx, QuartzSchedulerContext schedulerCtx) {
        final ServiceDiscoveryController localController = microCtx.getLocalController();
        final EventMethodDefinition definition = EventMethodDefinition.create("/scheduler", "/:service_id",
                                                                              ActionMethodMapping.CUD_MAP, false);
        localController.addEventMessageRecord("edge-scheduler", schedulerCtx.getRegisterModel().getAddress(),
                                              definition).subscribe();
    }

}
