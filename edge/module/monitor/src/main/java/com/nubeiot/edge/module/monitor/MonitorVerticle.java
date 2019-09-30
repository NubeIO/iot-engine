package com.nubeiot.edge.module.monitor;

import java.util.Collections;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.module.monitor.handlers.MonitorCpuUtilizationEventHandler;
import com.nubeiot.edge.module.monitor.handlers.MonitorDiskUtilizationEventHandler;
import com.nubeiot.edge.module.monitor.handlers.MonitorMemoryUtilizationEventHandler;
import com.nubeiot.edge.module.monitor.handlers.MonitorNetworkStatusEventHandler;
import com.nubeiot.edge.module.monitor.handlers.MonitorOsEventHandler;
import com.nubeiot.edge.module.monitor.handlers.MonitorStatusEventHandler;
import com.nubeiot.edge.module.monitor.handlers.MonitorUptimeEventHandler;
import com.nubeiot.eventbus.edge.installer.EdgeMonitorEventBus;

public final class MonitorVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        addProvider(new MicroserviceProvider(), this::publishServices);
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(EdgeMonitorEventBus.monitorStatus(true), new MonitorStatusEventHandler())
                   .register(EdgeMonitorEventBus.monitorCpuUtilization(true), new MonitorCpuUtilizationEventHandler())
                   .register(EdgeMonitorEventBus.monitorMemoryUtilization(true),
                             new MonitorMemoryUtilizationEventHandler())
                   .register(EdgeMonitorEventBus.monitorDiskUtilization(true), new MonitorDiskUtilizationEventHandler())
                   .register(EdgeMonitorEventBus.monitorUptime(true), new MonitorUptimeEventHandler())
                   .register(EdgeMonitorEventBus.monitorOs(true), new MonitorOsEventHandler())
                   .register(EdgeMonitorEventBus.monitorNetworkStatus(true), new MonitorNetworkStatusEventHandler());
    }

    private void publishServices(MicroContext microContext) {
        final ServiceDiscoveryController discovery = microContext.getLocalController();
        if (!discovery.isEnabled()) {
            return;
        }
        final ActionMethodMapping mapping = methodMapping();
        Flowable<Record> f1 = Single.merge(
            discovery.addEventMessageRecord("bios.monitor.status", EdgeMonitorEventBus.monitorStatus(true).getAddress(),
                                            EventMethodDefinition.create("/monitor/status", mapping)),
            discovery.addEventMessageRecord("bios.monitor.cpu-utilization",
                                            EdgeMonitorEventBus.monitorCpuUtilization(true).getAddress(),
                                            EventMethodDefinition.create("/monitor/cpu-utilization", mapping)),
            discovery.addEventMessageRecord("bios.monitor.memory-utilization",
                                            EdgeMonitorEventBus.monitorMemoryUtilization(true).getAddress(),
                                            EventMethodDefinition.create("/monitor/memory-utilization", mapping)),
            discovery.addEventMessageRecord("bios.monitor.disk-utilization",
                                            EdgeMonitorEventBus.monitorDiskUtilization(true).getAddress(),
                                            EventMethodDefinition.create("/monitor/disk-utilization", mapping)));

        Flowable<Record> f2 = Single.merge(
            discovery.addEventMessageRecord("bios.monitor.uptime", EdgeMonitorEventBus.monitorUptime(true).getAddress(),
                                            EventMethodDefinition.create("/monitor/uptime", mapping)),
            discovery.addEventMessageRecord("bios.monitor.os", EdgeMonitorEventBus.monitorOs(true).getAddress(),
                                            EventMethodDefinition.create("/monitor/os", mapping)),
            discovery.addEventMessageRecord("bios.monitor.network-status",
                                            EdgeMonitorEventBus.monitorNetworkStatus(true).getAddress(),
                                            EventMethodDefinition.create("/monitor/network", mapping)));

        f1.mergeWith(f2).subscribe();
    }

    private ActionMethodMapping methodMapping() {
        return () -> Collections.singletonMap(EventAction.GET_LIST, HttpMethod.GET);
    }

}
