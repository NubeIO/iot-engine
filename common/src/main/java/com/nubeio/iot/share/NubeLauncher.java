package com.nubeio.iot.share;

import com.nubeio.iot.share.utils.Configs;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class NubeLauncher extends io.vertx.core.Launcher {

    protected final Logger logger;
    private JsonObject config;

    public NubeLauncher() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public static void main(String[] args) {
        new NubeLauncher().dispatch(args);
    }

    @Override
    public void afterConfigParsed(JsonObject config) {
        logger.debug("Input config: {}", config.encode());
        this.config = loadDefaultConfig().mergeIn(config, true);
        logger.debug("Final config: {}", this.config);
        super.afterConfigParsed(this.config);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        if (!options.isClustered()) {
            options.setClustered(true);
        }
        logger.info("Before starting Vertx instance");
        logger.debug("Config: {}", config.encode());
        final JsonObject eventBusOption = config.getJsonObject("system", new JsonObject())
                                                .getJsonObject("eventBus", new JsonObject());
        logger.info("Update event bus configuration {}...", eventBusOption);
        options.setEventBusOptions(new EventBusOptions(eventBusOption));
        super.beforeStartingVertx(options);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        if (deploymentOptions.getConfig() == null) {
            deploymentOptions.setConfig(new JsonObject());
        }
        deploymentOptions.setConfig(this.config.mergeIn(deploymentOptions.getConfig(), true));
        super.beforeDeployingVerticle(deploymentOptions);
    }

    static JsonObject loadDefaultConfig() {
        return Configs.loadDefaultConfig("system.json");
    }

}
