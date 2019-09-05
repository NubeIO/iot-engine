package com.nubeiot.core.micro;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import com.nubeiot.core.micro.MicroConfig.LocalServiceDiscoveryConfig;

import lombok.NonNull;

class LocalSDController extends ServiceDiscoveryController {

    LocalSDController(Vertx vertx, LocalServiceDiscoveryConfig config, String sharedKey,
                      CircuitBreakerController circuitController) {
        super(config, sharedKey, createServiceDiscovery(vertx, config, ServiceDiscoveryKind.LOCAL, v -> true),
              circuitController);
    }

    @Override
    public <T extends ServiceGatewayAnnounceMonitor> void subscribe(EventBus eventBus, T announceMonitor) {
        eventBus.localConsumer(config.getAnnounceAddress(), announceMonitor);
    }

    @Override
    public <T extends ServiceGatewayUsageMonitor> void subscribe(EventBus eventBus, @NonNull T usageMonitor) {
        eventBus.localConsumer(config.getUsageAddress(), usageMonitor);
    }

    @Override
    ServiceDiscoveryKind kind() {
        return ServiceDiscoveryKind.LOCAL;
    }

    @Override
    String computeINet(String host) {
        return host;
    }

}
