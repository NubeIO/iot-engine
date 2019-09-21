package com.nubeiot.core.micro;

import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.micro.MicroConfig.GatewayConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class MicroContext extends UnitContext {

    @Getter
    private CircuitBreakerController breakerController;
    @Getter
    private ServiceDiscoveryController clusterController;
    @Getter
    private ServiceDiscoveryController localController;

    /**
     * For test only
     */
    MicroContext setup(Vertx vertx, MicroConfig config) {
        return setup(vertx, config, MicroContext.class.getName());
    }

    MicroContext setup(Vertx vertx, MicroConfig config, String sharedKey) {
        this.breakerController = CircuitBreakerController.create(vertx, config.getCircuitConfig());
        this.clusterController = new ClusterSDController(vertx, config.getDiscoveryConfig(), sharedKey,
                                                         this.breakerController);
        this.localController = new LocalSDController(vertx, config.getLocalDiscoveryConfig(), sharedKey,
                                                     this.breakerController);
        setupGateway(vertx, config.getGatewayConfig(), clusterController, localController, sharedKey);
        return this;
    }

    private void setupGateway(Vertx vertx, GatewayConfig config, ServiceDiscoveryController clusterController,
                              ServiceDiscoveryController localController, String sharedKey) {
        if (!config.isEnabled()) {
            logger.info("Skip setup service discovery gateway");
            return;
        }
        logger.info("Service Discovery Gateway Config : {}", config.toJson().encode());
        if (vertx.isClustered()) {
            clusterController.subscribe(vertx, config.getClusterAnnounceMonitorClass(),
                                        config.getClusterUsageMonitorClass());
        }
        localController.subscribe(vertx, config.getLocalAnnounceMonitorClass(), config.getLocalUsageMonitorClass());
        SharedDataDelegate.getEventController(vertx, sharedKey)
                          .register(config.getIndexAddress(), new ServiceGatewayIndex(this));
    }

    void unregister(Future future) {
        this.clusterController.unregister(future);
        this.localController.unregister(future);
    }

    public void rescanService(EventBus eventBus) {
        if (Objects.nonNull(clusterController)) {
            clusterController.rescanService(eventBus);
        }
        if (Objects.nonNull(localController)) {
            localController.rescanService(eventBus);
        }
    }

}
