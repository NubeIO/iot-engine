package com.nubeio.iot.share;

import java.util.Objects;

import com.hazelcast.config.Config;
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

    private static final Logger logger = LoggerFactory.getLogger(NubeLauncher.class);
    private static final String SYSTEM_CFG_KEY = "system";
    private static final String DEPLOY_CFG_KEY = "deploy";
    private static final String EVENT_BUS_CFG_KEY = "eventBus";
    private static final String CLUSTER_CFG_KEY = "cluster";
    private JsonObject allConfig;
    private VertxOptions options;

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        new NubeLauncher().dispatch(args);
    }

    @Override
    public void afterConfigParsed(JsonObject config) {
        logger.debug("Input config: {}", config.encode());
        this.allConfig = defaultConfig().mergeIn(config, true);
        logger.debug("Final config: {}", this.allConfig);
        super.afterConfigParsed(this.allConfig);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        logger.info("Before starting Vertx instance");
        final JsonObject cfg = getSystemCfg(allConfig);
        logger.debug("System Config: {}", cfg.encode());
        this.options = loadVertxOption(options, cfg);
        super.beforeStartingVertx(this.options);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        logger.info("Before deploying Vertx instance");
        JsonObject cfg = Objects.isNull(deploymentOptions.getConfig())
                         ? getDeployCfg(allConfig)
                         : getDeployCfg(allConfig).mergeIn(deploymentOptions.getConfig(), true);
        logger.debug("Deployment Config: {}", cfg.encode());
        super.beforeDeployingVerticle(deploymentOptions.setConfig(cfg));
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

    static JsonObject getSystemCfg(JsonObject config) {
        return config.getJsonObject(SYSTEM_CFG_KEY, new JsonObject());
    }

    static JsonObject getDeployCfg(JsonObject config) {
        return config.getJsonObject(DEPLOY_CFG_KEY, new JsonObject());
    }

    private static VertxOptions loadVertxOption(VertxOptions vertxOptions, JsonObject systemCfg) {
        configEventBus(vertxOptions, systemCfg.getJsonObject(EVENT_BUS_CFG_KEY, new JsonObject()));
        configCluster(vertxOptions, systemCfg.getJsonObject(CLUSTER_CFG_KEY, new JsonObject()));
        return vertxOptions;
    }

    private static void configEventBus(VertxOptions options, JsonObject eventBusCfg) {
        logger.info("Update event bus configuration {}...", eventBusCfg);
        options.setEventBusOptions(new EventBusOptions(eventBusCfg));
    }

    private static void configCluster(VertxOptions options, JsonObject clusterOption) {
        if (clusterOption.isEmpty() || !clusterOption.getBoolean("active", Boolean.FALSE)) {
            return;
        }
        options.setClustered(true);
        options.setHAEnabled(clusterOption.getBoolean("ha_enabled", Boolean.FALSE));
        logger.info("Update cluster configuration {}...", clusterOption);
        Config clusterCfg = Configs.parseClusterConfig(clusterOption)
                                   .build()
                                   .setProperty("hazelcast.logging.type", "slf4j");
        logger.info(clusterCfg.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
        options.setClusterManager(new HazelcastClusterManager(clusterCfg));
    }

}
