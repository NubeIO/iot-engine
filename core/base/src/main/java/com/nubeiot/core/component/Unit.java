package com.nubeiot.core.component;

import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.Configs;

import lombok.NonNull;

/**
 * Represents small and independent component that integrate with verticle.
 *
 * @see IConfig
 * @see UnitVerticle
 */
public interface Unit<C extends IConfig> extends Verticle {

    /**
     * Config class
     *
     * @return IConfig class
     */
    @NonNull Class<C> configClass();

    /**
     * Define a config file in classpath
     *
     * @return config file path
     */
    @NonNull String configFile();

    default C computeConfig(JsonObject config) {
        return IConfig.merge(IConfig.from(Configs.loadJsonConfig(configFile()), configClass()), config, configClass());
    }

}
