package com.nubeiot.core.component;

import java.util.function.Function;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * SharedData delegate
 */
public interface SharedDataDelegate {

    /**
     * Data key for EventBus controller
     *
     * @see EventController
     */
    String SHARED_EVENTBUS = "EVENTBUS_CONTROLLER";
    String SHARED_DATADIR = "DATADIR";

    @SuppressWarnings("unchecked")
    static <D> D getSharedDataValue(Function<String, Object> sharedDataFunc, String dataKey) {
        final Logger logger = LoggerFactory.getLogger(SharedDataDelegate.class);
        try {
            logger.debug("Shared Data Key: \"{}\"", dataKey);
            return (D) sharedDataFunc.apply(dataKey);
        } catch (ClassCastException e) {
            logger.warn("Data value Type is not matching with expected data key {}", e, dataKey);
            return null;
        }
    }

    static <D> D getLocalDataValue(@NonNull Vertx vertx, String sharedKey, String dataKey) {
        return SharedDataDelegate.getSharedDataValue(
            k -> vertx.sharedData().getLocalMap(Strings.requireNotBlank(sharedKey)).get(k), dataKey);
    }

    static <D> void addLocalDataValue(@NonNull Vertx vertx, String sharedKey, String dataKey, D data) {
        vertx.sharedData().getLocalMap(Strings.requireNotBlank(sharedKey)).put(Strings.requireNotBlank(dataKey), data);
    }

    /**
     * Get shared data value by data key
     *
     * @param dataKey Given data key
     * @param <D>     Type of data value
     * @return Data value. It may be {@code null} if no data value by key or data value type doesn't match type with
     *     expected value
     */
    <D> D getSharedDataValue(String dataKey);

    /**
     * System will register it automatically. You don't need call it directly
     *
     * @param <T>            Type of instance that inherited from {@link SharedDataDelegate}
     * @param sharedDataFunc Given shared data function from {@code Vertx Verticle}
     * @return a reference to this, so the API can be used fluently
     */
    <T extends SharedDataDelegate> T registerSharedData(@NonNull Function<String, Object> sharedDataFunc);

    abstract class AbstractSharedDataDelegate implements SharedDataDelegate {

        private Function<String, Object> sharedDataFunc;

        @Override
        public final <D> D getSharedDataValue(String dataKey) {
            return SharedDataDelegate.getSharedDataValue(this.sharedDataFunc, dataKey);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final <T extends SharedDataDelegate> T registerSharedData(
            @NonNull Function<String, Object> sharedDataFunc) {
            this.sharedDataFunc = sharedDataFunc;
            return (T) this;
        }

    }

}
