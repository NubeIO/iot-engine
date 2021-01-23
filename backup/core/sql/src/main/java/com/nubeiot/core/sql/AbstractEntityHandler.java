package com.nubeiot.core.sql;

import java.nio.file.Path;

import org.jooq.Configuration;
import org.jooq.DSLContext;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents for Abstract entity handler.
 *
 * @since 1.0.0
 */
public abstract class AbstractEntityHandler implements EntityHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @NonNull
    final Configuration jooqConfig;
    @NonNull
    private final Vertx vertx;
    @Getter(value = AccessLevel.PROTECTED)
    private String sharedKey = getClass().getName();

    /**
     * Instantiates a new Abstract entity handler.
     *
     * @param jooqConfig the jooq config
     * @param vertx      the vertx
     * @since 1.0.0
     */
    public AbstractEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        this.jooqConfig = jooqConfig;
        this.vertx = vertx;
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public EventbusClient eventClient() {
        return SharedDataDelegate.getEventController(vertx, sharedKey);
    }

    @Override
    public Path dataDir() {
        return SharedDataDelegate.getDataDir(vertx, sharedKey);
    }

    @Override
    public <D> D sharedData(String dataKey) {
        return SharedDataDelegate.getLocalDataValue(vertx, sharedKey, dataKey);
    }

    @Override
    public <D> D addSharedData(String dataKey, D data) {
        return SharedDataDelegate.addLocalDataValue(vertx, sharedKey, dataKey, data);
    }

    @Override
    public DSLContext dsl() {
        return jooqConfig.dsl();
    }

    @SuppressWarnings("unchecked")
    <T extends EntityHandler> T registerSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        return (T) this;
    }

}
