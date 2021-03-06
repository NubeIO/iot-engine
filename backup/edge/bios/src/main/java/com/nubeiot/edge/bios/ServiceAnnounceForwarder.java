package com.nubeiot.edge.bios;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.micro.monitor.ServiceGatewayAnnounceMonitor;
import com.nubeiot.eventbus.edge.gateway.GatewayEventBus;

final class ServiceAnnounceForwarder extends ServiceGatewayAnnounceMonitor {

    protected ServiceAnnounceForwarder(Vertx vertx, ServiceDiscoveryController controller, String sharedKey) {
        super(vertx, controller, sharedKey);
    }

    @Override
    protected void handle(Record record) {
        EventbusClient eventClient = SharedDataDelegate.getEventController(getVertx(), getSharedKey());
        eventClient.fire(DeliveryEvent.from(GatewayEventBus.ROUTER_ANNOUNCEMENT, EventAction.MONITOR,
                                            RequestData.builder().body(record.toJson()).build().toJson()));
    }

}
