package com.nubeiot.edge.module.monitor;

import java.util.HashMap;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.edge.module.monitor.handlers.MonitorNetworkStatusEventHandler;
import com.nubeiot.edge.module.monitor.handlers.MonitorStatusEventHandler;
import com.nubeiot.eventbus.edge.EdgeMonitorEventBus;

public class MonitorVerticle extends ContainerVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start() {
        logger.info("Starting Service Monitor Vert.x");
        super.start();
        addProvider(new MicroserviceProvider(), this::publishServices);
    }

    @Override
    public void registerEventbus(EventController controller) {
        boolean local = this.nubeConfig.getAppConfig().toJson().getBoolean("deviceLocal", false);
        controller.register(EdgeMonitorEventBus.getMonitorStatus(local), new MonitorStatusEventHandler());
        controller.register(EdgeMonitorEventBus.getMonitorNetworkStatus(local), new MonitorNetworkStatusEventHandler());
    }

    private void publishServices(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("monitor-status", EdgeMonitorEventBus.getMonitorStatus(true).getAddress(),
                                           EventMethodDefinition.create("/monitor/status",
                                                                        getListActionMethodMapping()))
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("monitor-network-status",
                                           EdgeMonitorEventBus.getMonitorNetworkStatus(true).getAddress(),
                                           EventMethodDefinition.create("/monitor/network/status",
                                                                        getListActionMethodMapping()))
                    .subscribe();
    }

    private ActionMethodMapping getListActionMethodMapping() {
        return () -> new HashMap<EventAction, HttpMethod>() {
            {
                put(EventAction.GET_LIST, HttpMethod.GET);
            }
        };
    }

}
