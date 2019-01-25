package com.nubeiot.core.component;

import java.util.function.Consumer;

import io.vertx.core.Future;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;

/**
 * Represents a container consists a list of {@code Verticle unit} to startup application
 *
 * @see Unit
 * @see HasConfig
 * @see ContainerVerticle
 */
public interface Container extends HasConfig<NubeConfig> {

    @Override
    default Class<NubeConfig> configClass() {
        return NubeConfig.class;
    }

    @Override
    default String configFile() {
        return "config.json";
    }

    /**
     * Register eventbus consumer
     *
     * @param controller EventController
     * @see EventController#register(EventModel, EventHandler)
     * @see EventController#register(String, EventHandler)
     * @see EventController#register(String, boolean, EventHandler)
     */
    void registerEventbus(EventController controller);

    /**
     * Add local shared data to between different verticles
     *
     * @param key  Data key
     * @param data Data value
     * @return a reference to this, so the API can be used fluently
     */
    Container addSharedData(String key, Object data);

    /**
     * Add unit provider to startup
     *
     * @param <T>      Type of unit
     * @param provider Unit provider
     * @return a reference to this, so the API can be used fluently
     */
    <T extends Unit> Container addProvider(UnitProvider<T> provider);

    /**
     * Add unit provider to startup
     *
     * @param <T>            Type of unit
     * @param provider       Unit provider
     * @param successHandler Success handler after system start component successfully
     * @return a reference to this, so the API can be used fluently
     */
    <C extends UnitContext, T extends Unit> Container addProvider(UnitProvider<T> provider, Consumer<C> successHandler);

    /**
     * Start a list of register unit verticle based on the order of given providers of {@link
     * #addProvider(UnitProvider)} or {@link #addProvider(UnitProvider, Consumer)}
     * <p>
     * If any unit verticle starts failed, future will catch and report it to {@code Vertx}
     *
     * @param future a future which should be called when all unit verticle start-up is complete.
     */
    void startUnits(Future<Void> future);

    /**
     * Stop a list of register units
     *
     * @param future a future which should be called when all unit verticle clean-up is complete.
     */
    void stopUnits(Future<Void> future);

}
