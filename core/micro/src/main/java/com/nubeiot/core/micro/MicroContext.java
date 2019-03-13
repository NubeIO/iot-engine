package com.nubeiot.core.micro;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.micro.MicroConfig.GatewayConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class MicroContext extends UnitContext {

    private static final Logger logger = LoggerFactory.getLogger(MicroContext.class);

    @Getter
    private CircuitBreakerController breakerController;
    @Getter
    private ServiceDiscoveryController clusterController;
    @Getter
    private ServiceDiscoveryController localController;

    MicroContext create(io.vertx.core.Vertx vertx, MicroConfig config) {
        create(Vertx.newInstance(vertx), config);
        return this;
    }

    private void create(Vertx vertx, MicroConfig config) {
        this.breakerController = CircuitBreakerController.create(vertx, config.getCircuitConfig());
        this.clusterController = new ClusterSDController(vertx, config.getDiscoveryConfig(), this.breakerController);
        this.localController = new LocalSDController(vertx, config.getLocalDiscoveryConfig(), this.breakerController);
        setupGateway(vertx, config.getGatewayConfig(), clusterController, localController);
    }

    private void setupGateway(Vertx vertx, GatewayConfig config, ServiceDiscoveryController clusterController,
                              ServiceDiscoveryController localController) {
        if (!config.isEnabled()) {
            logger.info("Skip setup service discovery gateway");
            return;
        }
        logger.info("Service Discovery Gateway Config : {}", config.toJson().encode());
        EventBus eventBus = vertx.getDelegate().eventBus();
        if (vertx.isClustered()) {
            clusterController.subscribe(eventBus, config.getClusterAnnounceMonitorClass(),
                                        config.getClusterUsageMonitorClass());
        }
        localController.subscribe(eventBus, config.getLocalAnnounceMonitorClass(), config.getLocalUsageMonitorClass());
    }

    void unregister(Future future) {
        this.clusterController.unregister(future);
        this.localController.unregister(future);
    }

}
