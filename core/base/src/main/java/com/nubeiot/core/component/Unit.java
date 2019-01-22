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

    /**
     * Compute configure based on user input configuration and default unit configuration that defined in {@link
     * #configFile()}
     *
     * @param config given user configuration
     * @return config instance
     * @see IConfig
     */
    default C computeConfig(JsonObject config) {
        return IConfig.merge(IConfig.from(Configs.loadJsonConfig(configFile()), configClass()), config, configClass());
    }

    /**
     * Register {@code Vertx} local shared data between {@code Container} and {@code unit}
     * <p>
     * This method will be called automatically by system before deploying verticle.
     *
     * @param sharedKey shared data key map
     * @return a reference to this, so the API can be used fluently
     * @see Container
     */
    Unit<C> registerSharedData(String sharedKey);

    /**
     * Retrieve {@code Vertx} shared data value by key data
     *
     * @param dataKey given data key
     * @param <T>     T type of data value
     * @return Data value ({@code nullable})
     * @throws ClassCastException if the object is not null and is not assignable to the type {@code T}.
     */
    <T> T getSharedData(String dataKey);

}
