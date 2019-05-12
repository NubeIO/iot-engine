package com.nubeiot.core.micro;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.servicediscovery.Status;

import com.nubeiot.core.micro.MicroConfig.LocalServiceDiscoveryConfig;

import lombok.NonNull;

class LocalSDController extends ServiceDiscoveryController {

    LocalSDController(Vertx vertx, LocalServiceDiscoveryConfig config, String sharedKey,
                      CircuitBreakerController circuitController) {
        super(config, sharedKey, createServiceDiscovery(vertx, config, "Local", v -> true), circuitController);
    }

    @Override
    public <T extends ServiceGatewayAnnounceMonitor> void subscribe(EventBus eventBus, T announceMonitor) {
        eventBus.localConsumer(config.getAnnounceAddress(), announceMonitor);
        eventBus.send(config.getAnnounceAddress(), new JsonObject().put("status", Status.OUT_OF_SERVICE));
    }

    @Override
    public <T extends ServiceGatewayUsageMonitor> void subscribe(EventBus eventBus, @NonNull T usageMonitor) {
        eventBus.localConsumer(config.getUsageAddress(), usageMonitor);
    }

    @Override
    String kind() {
        return "Local";
    }

    @Override
    String computeINet(String host) {
        return host;
    }

}
