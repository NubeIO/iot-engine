package com.nubeiot.core.micro;

import io.github.zero.utils.Strings;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import com.nubeiot.core.micro.MicroConfig.ServiceDiscoveryConfig;
import com.nubeiot.core.micro.monitor.ServiceGatewayAnnounceMonitor;
import com.nubeiot.core.micro.monitor.ServiceGatewayUsageMonitor;
import com.nubeiot.core.utils.Networks;

import lombok.NonNull;

final class ClusterSDController extends ServiceDiscoveryController {

    ClusterSDController(Vertx vertx, ServiceDiscoveryConfig config, String sharedKey,
                        CircuitBreakerController circuitController) {
        super(config, sharedKey, createServiceDiscovery(vertx, config, ServiceKind.CLUSTER, Vertx::isClustered),
              circuitController);
    }

    @Override
    <T extends ServiceGatewayAnnounceMonitor> void subscribe(EventBus eventBus, @NonNull T announceMonitor) {
        eventBus.consumer(config.getAnnounceAddress(), announceMonitor);
    }

    @Override
    <T extends ServiceGatewayUsageMonitor> void subscribe(EventBus eventBus, @NonNull T usageMonitor) {
        if (Strings.isNotBlank(config.getUsageAddress())) {
            eventBus.consumer(config.getUsageAddress(), usageMonitor);
        }
    }

    @Override
    ServiceKind kind() {
        return ServiceKind.CLUSTER;
    }

    @Override
    String computeINet(String host) {
        return Networks.computeNATAddress(host);
    }

}
