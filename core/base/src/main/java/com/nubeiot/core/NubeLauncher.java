package com.nubeiot.core;

import java.util.Objects;

import com.nubeiot.core.cluster.ClusterNodeListener;
import com.nubeiot.core.cluster.ClusterRegistry;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.eventbus.EventBus;

public final class NubeLauncher extends io.vertx.core.Launcher {

    private static final Logger logger;
    private NubeConfig config;
    private VertxOptions options;
    private IClusterDelegate clusterDelegate;

    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        logger = LoggerFactory.getLogger(NubeLauncher.class);
    }

    public static void main(String[] args) {
        new NubeLauncher().dispatch(args);
    }

    @Override
    public void afterConfigParsed(JsonObject config) {
        logger.debug("Input config: {}", config.encode());
        JsonObject jsonCfg = Configs.loadJsonConfig("system.json").mergeIn(config, true);
        this.config = IConfig.from(jsonCfg, NubeConfig.class);
        logger.debug("Final all config: {}", jsonCfg.encode());
        super.afterConfigParsed(jsonCfg);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        logger.info("Before starting Vertx instance...");
        JsonObject systemConfig = config.getSystemConfig().toJson();
        logger.info("System Config: {}", systemConfig.encode());
        this.options = reloadVertxOptions(options);
        super.beforeStartingVertx(this.options);
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        if (vertx.isClustered()) {
            String address = config.getSystemConfig().getClusterConfig().getListenerAddress();
            if (Strings.isNotBlank(address)) {
                EventBus eventBus = new EventBus(vertx.eventBus());
                this.options.getClusterManager()
                            .nodeListener(new ClusterNodeListener(clusterDelegate, eventBus, address));
            }
        }
        super.afterStartingVertx(vertx);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        logger.info("Before deploying Vertx instance...");
        JsonObject inputDeployOptions = deploymentOptions.toJson();
        JsonObject inputAppCfg = deploymentOptions.getConfig();
        logger.debug("Input Deployment Options: {}", inputDeployOptions.encode());
        JsonObject deployOptions = config.toJson();
        DeploymentOptions mergeOptions = new DeploymentOptions(deployOptions.mergeIn(inputDeployOptions, true));
        JsonObject appConfig = config.getAppConfig().toJson();
        mergeOptions.setConfig(appConfig.mergeIn(inputAppCfg, true));
        logger.info("Final Deployment Options: {}", mergeOptions.toJson().encode());
        super.beforeDeployingVerticle(mergeOptions);
    }

    @Override
    public void afterStoppingVertx() {
        this.options.getClusterManager().leave(event -> {
            if (event.failed()) {
                logger.error("Failed to leave cluster", event.cause());
            }
        });
        super.afterStoppingVertx();
    }

    private VertxOptions reloadVertxOptions(VertxOptions vertxOptions) {
        StateMachine.init();
        ClusterRegistry.init();
        configEventBus(vertxOptions);
        configCluster(vertxOptions);
        return vertxOptions;
    }

    private void configEventBus(VertxOptions options) {
        logger.info("Setup EventBus...");
        EventBusOptions option = this.config.getSystemConfig().getEventBusConfig();
        logger.info("Event Bus Options: {}", option.toJson().encode());
        options.setEventBusOptions(option);
    }

    private void configCluster(VertxOptions options) {
        NubeConfig.SystemConfig.ClusterConfig clusterCfg = config.getSystemConfig().getClusterConfig();
        if (Objects.isNull(clusterCfg) || !clusterCfg.isActive()) {
            return;
        }
        logger.info("Setup Cluster...");
        options.setClustered(true);
        options.setHAEnabled(clusterCfg.isHa());
        ClusterType type = clusterCfg.getType();
        this.clusterDelegate = ClusterRegistry.instance().getClusterDelegate(type);
        if (Objects.isNull(this.clusterDelegate)) {
            throw new EngineException("Cannot load cluster config: " + type);
        }
        options.setClusterManager(this.clusterDelegate.initClusterManager(clusterCfg));
    }

}
