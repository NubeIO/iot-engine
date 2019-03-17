package com.nubeiot.core.micro;

import io.vertx.core.eventbus.EventBus;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.micro.MicroConfig.LocalServiceDiscoveryConfig;
import com.nubeiot.core.utils.Strings;

class LocalSDController extends ServiceDiscoveryController {

    LocalSDController(Vertx vertx, LocalServiceDiscoveryConfig config, String sharedKey,
                      CircuitBreakerController circuitController) {
        super(config, sharedKey, createServiceDiscovery(vertx, config, "Local", v -> true), circuitController);
    }

    @Override
    String kind() {
        return "Local";
    }

    @Override
    void subscribe(EventBus eventBus, String announceMonitorClass, String usageMonitorClass) {
        eventBus.localConsumer(config.getAnnounceAddress(), ServiceGatewayAnnounceMonitor.create(announceMonitorClass));
        if (Strings.isNotBlank(config.getUsageAddress())) {
            eventBus.localConsumer(config.getUsageAddress(), ServiceGatewayUsageMonitor.create(usageMonitorClass));
        }
    }

}
