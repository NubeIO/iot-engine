package com.nubeiot.core.component;

import io.vertx.core.Verticle;

import com.nubeiot.core.IConfig;

/**
 * Represents small and independent component that integrate with verticle.
 *
 * @param <C> Type of Config
 * @param <T> Type of Unit Context
 * @see IConfig
 * @see UnitContext
 * @see HasConfig
 * @see UnitVerticle
 */
public interface Unit<C extends IConfig, T extends UnitContext> extends HasConfig<C>, Verticle {

    /**
     * Unit context
     *
     * @return UnitContext
     */
    T getContext();

    /**
     * Register {@code Vertx} local shared data key between {@code Container} and {@code unit}
     * <p>
     * This method will be called automatically by system before deploying verticle.
     *
     * @param sharedKey shared data key
     * @param <U>       Type of Unit Verticle
     * @return a reference to this, so the API can be used fluently
     * @see Container
     */
    <U extends Unit<C, T>> U registerSharedKey(String sharedKey);

    /**
     * Retrieve {@code Vertx} shared data value by key data
     *
     * @param <R>     T type of data value
     * @param dataKey given data key
     * @return Data value. {@code nullable} if no data value by key or data value type doesn't match type with expected
     *     value
     */
    default <R> R getSharedData(String dataKey) {
        return getSharedData(dataKey, null);
    }

    /**
     * Retrieve {@code Vertx} shared data value by key data. If no data value by key or data value type doesn't match
     * type with expected value, it will fallback to given value.
     *
     * @param <R>      T type of data value
     * @param dataKey  given data key
     * @param fallback Fallback value
     * @return Data value.
     */
    <R> R getSharedData(String dataKey, R fallback);

}
