package com.nubeiot.core.micro;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.servicediscovery.Status;

import com.nubeiot.core.micro.MicroConfig.ServiceDiscoveryConfig;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

class ClusterSDController extends ServiceDiscoveryController {

    ClusterSDController(Vertx vertx, ServiceDiscoveryConfig config, String sharedKey,
                        CircuitBreakerController circuitController) {
        super(config, sharedKey, createServiceDiscovery(vertx, config, "Cluster", Vertx::isClustered),
              circuitController);
    }

    @Override
    <T extends ServiceGatewayAnnounceMonitor> void subscribe(EventBus eventBus, @NonNull T announceMonitor) {
        eventBus.consumer(config.getAnnounceAddress(), announceMonitor);
        eventBus.send(config.getAnnounceAddress(), new JsonObject().put("status", Status.UNKNOWN));
    }

    @Override
    <T extends ServiceGatewayUsageMonitor> void subscribe(EventBus eventBus, @NonNull T usageMonitor) {
        if (Strings.isNotBlank(config.getUsageAddress())) {
            eventBus.consumer(config.getUsageAddress(), usageMonitor);
        }
    }

    @Override
    String kind() {
        return "Cluster";
    }

    @Override
    String computeINet(String host) {
        return Networks.computeNATAddress(host);
    }

}
