package com.nubeio.iot.share;

import com.hazelcast.config.Config;
import com.nubeio.iot.share.statemachine.StateMachine;
import com.nubeio.iot.share.utils.Configs;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public final class NubeLauncher extends io.vertx.core.Launcher {

    private static final Logger logger;
    private JsonObject allConfig;
    private VertxOptions options;

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
        this.allConfig = defaultConfig().mergeIn(config, true);
        logger.info("Final all config: {}", this.allConfig);
        super.afterConfigParsed(this.allConfig);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        logger.info("Before starting Vertx instance...");
        JsonObject cfg = Configs.getSystemCfg(allConfig);
        logger.info("System Config: {}", cfg.encode());
        this.options = loadVertxOption(options, cfg);
        StateMachine.init();
        super.beforeStartingVertx(this.options);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        logger.info("Before deploying Vertx instance...");
        JsonObject inputDeployOptions = deploymentOptions.toJson();
        JsonObject inputAppCfg = deploymentOptions.getConfig();
        logger.debug("Input Deployment Options: {}", inputDeployOptions.encode());
        JsonObject options = Configs.getDeployCfg(allConfig).mergeIn(inputDeployOptions, true);
        DeploymentOptions mergeOptions = new DeploymentOptions(options);
        mergeOptions.setConfig(Configs.getApplicationCfg(allConfig).mergeIn(inputAppCfg, true));
        logger.info("Final Deployment Options: {}", mergeOptions.toJson().encode());
        super.beforeDeployingVerticle(mergeOptions);
    }

    @Override
    public void beforeStoppingVertx(Vertx vertx) {
        this.options.getClusterManager().leave(event -> {
            if (event.failed()) {
                logger.error("Failed to leave cluster", event.cause());
            }
        });
        super.beforeStoppingVertx(vertx);
    }

    static JsonObject defaultConfig() {
        return Configs.loadDefaultConfig("system.json");
    }

    static VertxOptions defaultVertxOption(JsonObject config) {
        return loadVertxOption(new VertxOptions(), config);
    }

    private static VertxOptions loadVertxOption(VertxOptions vertxOptions, JsonObject systemCfg) {
        configEventBus(vertxOptions, systemCfg.getJsonObject(Configs.EVENT_BUS_CFG_KEY, new JsonObject()));
        configCluster(vertxOptions, systemCfg.getJsonObject(Configs.CLUSTER_CFG_KEY, new JsonObject()));
        return vertxOptions;
    }

    private static void configEventBus(VertxOptions options, JsonObject eventBusCfg) {
        logger.info("Setup EventBus...");
        EventBusOptions option = new EventBusOptions(eventBusCfg);
        logger.debug("Event Bus Config: {}", eventBusCfg.encode());
        logger.info("Event Bus Options: {}", option.toJson().encode());
        options.setEventBusOptions(option);
    }

    private static void configCluster(VertxOptions options, JsonObject clusterConfig) {
        if (clusterConfig.isEmpty() || !clusterConfig.getBoolean("active", Boolean.FALSE)) {
            return;
        }
        logger.info("Setup Cluster...");
        options.setClustered(true);
        options.setHAEnabled(clusterConfig.getBoolean("ha", Boolean.FALSE));
        logger.info("Cluster Configuration: {}", clusterConfig);
        Config clusterCfg = Configs.parseClusterConfig(clusterConfig)
                                   .build()
                                   .setProperty("hazelcast.logging.type", "slf4j");
        options.setClusterManager(new HazelcastClusterManager(clusterCfg));
    }

}
