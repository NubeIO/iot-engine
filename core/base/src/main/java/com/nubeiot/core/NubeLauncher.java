package com.nubeiot.core;

import java.util.Objects;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.cluster.ClusterNodeListener;
import com.nubeiot.core.cluster.ClusterRegistry;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.EngineException;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.core.utils.Strings;

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
        logger.info("Parsing and merging configuration...");
        logger.debug("CONFIG::INPUT: {}", config.encode());
        this.config = IConfig.merge(Configs.loadJsonConfig("system.json"), config, NubeConfig.class);
        JsonObject cfg = this.config.toJson();
        logger.debug("CONFIG::FINAL: {}", cfg.encode());
        super.afterConfigParsed(cfg);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        logger.info("Before starting Vertx instance...");
        this.options = reloadVertxOptions(options);
        super.beforeStartingVertx(this.options);
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        if (vertx.isClustered()) {
            String addr = config.getSystemConfig().getClusterConfig().getListenerAddress();
            if (Strings.isNotBlank(addr)) {
                this.options.getClusterManager()
                            .nodeListener(new ClusterNodeListener(clusterDelegate, new EventController(vertx), addr));
            }
        }
        super.afterStartingVertx(vertx);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deployOptions) {
        logger.info("Before deploying Verticle...");
        logger.info("Merging configuration...");
        DeploymentOptions options = mergeDeployConfig(deployOptions).setConfig(mergeAppConfig(deployOptions).toJson());
        logger.info("CONFIG::FINAL DEPLOYMENT OPTIONS: {}", options.toJson().encode());
        super.beforeDeployingVerticle(options);
    }

    private NubeConfig.DeployConfig mergeDeployConfig(DeploymentOptions deploymentOptions) {
        JsonObject input = deploymentOptions.toJson();
        input.remove("config");
        logger.debug("CONFIG::INPUT DEPLOYMENT CFG: {}", input.encode());
        logger.debug("CONFIG::CURRENT DEPLOYMENT CFG: {}", config.getDeployConfig().toJson().encode());
        return IConfig.merge(config.getDeployConfig(), input, NubeConfig.DeployConfig.class);
    }

    private NubeConfig.AppConfig mergeAppConfig(DeploymentOptions deploymentOptions) {
        JsonObject input = deploymentOptions.getConfig();
        logger.debug("CONFIG::INPUT APP CFG: {}", input.encode());
        logger.debug("CONFIG::CURRENT APP CFG: {}", config.getAppConfig().toJson().encode());
        return IConfig.merge(config.getAppConfig(), input, NubeConfig.AppConfig.class);
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
        EventBusOptions option = this.config.getSystemConfig().getEventBusConfig().getOptions();
        logger.info("Configure EventBus with options: {}", option.toJson().encode());
        options.setEventBusOptions(option);
    }

    private void configCluster(VertxOptions options) {
        logger.info("Setup Cluster...");
        NubeConfig.SystemConfig.ClusterConfig clusterCfg = config.getSystemConfig().getClusterConfig();
        if (Objects.isNull(clusterCfg) || !clusterCfg.isActive()) {
            logger.info("Cluster is not activated");
            return;
        }
        logger.info("Configure Cluster with options: {}", clusterCfg.toJson());
        logger.info("Cluster type: {}", clusterCfg.getType());
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
