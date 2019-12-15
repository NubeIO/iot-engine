package com.nubeiot.edge.module.gateway.mock;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.micro.monitor.ServiceGatewayAnnounceMonitor;
import com.nubeiot.eventbus.edge.gateway.GatewayEventBus;

public final class MockAnnounceForwarder extends ServiceGatewayAnnounceMonitor {

    protected MockAnnounceForwarder(Vertx vertx, ServiceDiscoveryController controller, String sharedKey) {
        super(vertx, controller, sharedKey);
    }

    @Override
    protected void handle(Record r) {
        EventbusClient client = SharedDataDelegate.getEventController(getVertx(), getSharedKey());
        client.send(GatewayEventBus.ROUTER_ANNOUNCEMENT.getAddress(),
                    EventMessage.initial(EventAction.MONITOR, RequestData.builder().body(r.toJson()).build().toJson()));
    }

}
