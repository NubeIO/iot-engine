package com.nubeiot.core.micro;

import io.vertx.core.eventbus.EventBus;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.micro.MicroConfig.ServiceDiscoveryConfig;
import com.nubeiot.core.utils.Strings;

class ClusterSDController extends ServiceDiscoveryController {

    ClusterSDController(Vertx vertx, ServiceDiscoveryConfig config, String sharedKey,
                        CircuitBreakerController circuitController) {
        super(config, sharedKey, createServiceDiscovery(vertx, config, "Cluster", Vertx::isClustered),
              circuitController);
    }

    @Override
    String kind() {
        return "Cluster";
    }

    @Override
    void subscribe(EventBus eventBus, String announceMonitorClass, String usageMonitorClass) {
        eventBus.consumer(config.getAnnounceAddress(), ServiceGatewayAnnounceMonitor.create(announceMonitorClass));
        if (Strings.isNotBlank(config.getUsageAddress())) {
            eventBus.consumer(config.getUsageAddress(), ServiceGatewayUsageMonitor.create(usageMonitorClass));
        }
    }

}
