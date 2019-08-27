package com.nubeiot.edge.module.monitor;

import java.util.Collections;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.module.monitor.handlers.MonitorNetworkStatusEventHandler;
import com.nubeiot.edge.module.monitor.handlers.MonitorStatusEventHandler;
import com.nubeiot.eventbus.edge.installer.EdgeMonitorEventBus;

public final class MonitorVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        addProvider(new MicroserviceProvider(), this::publishServices);
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(EdgeMonitorEventBus.getMonitorStatus(true), new MonitorStatusEventHandler())
                   .register(EdgeMonitorEventBus.getMonitorNetworkStatus(true), new MonitorNetworkStatusEventHandler());
    }

    private void publishServices(MicroContext microContext) {
        final ServiceDiscoveryController discovery = microContext.getLocalController();
        if (!discovery.isEnabled()) {
            return;
        }
        final ActionMethodMapping mapping = methodMapping();
        Single.merge(discovery.addEventMessageRecord("bios.monitor.status",
                                                     EdgeMonitorEventBus.getMonitorStatus(true).getAddress(),
                                                     EventMethodDefinition.create("/monitor/status", mapping)),
                     discovery.addEventMessageRecord("bios.monitor.network-status",
                                                     EdgeMonitorEventBus.getMonitorNetworkStatus(true).getAddress(),
                                                     EventMethodDefinition.create("/monitor/network/status", mapping)))
              .subscribe();
    }

    private ActionMethodMapping methodMapping() {
        return () -> Collections.singletonMap(EventAction.GET_LIST, HttpMethod.GET);
    }

}
