package com.nubeiot.edge.module.gateway.mock;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.micro.monitor.ServiceGatewayAnnounceMonitor;
import com.nubeiot.eventbus.edge.gateway.GatewayEventBus;

public final class MockAnnounceForwarder extends ServiceGatewayAnnounceMonitor {

    protected MockAnnounceForwarder(Vertx vertx, ServiceDiscoveryController controller, String sharedKey) {
        super(vertx, controller, sharedKey);
    }

    @Override
    protected void handle(Record record) {
        EventController eventClient = SharedDataDelegate.getEventController(getVertx(), getSharedKey());
        eventClient.request(DeliveryEvent.from(GatewayEventBus.ROUTER_ANNOUNCEMENT, EventAction.MONITOR,
                                               RequestData.builder().body(record.toJson()).build().toJson()));
    }

}
