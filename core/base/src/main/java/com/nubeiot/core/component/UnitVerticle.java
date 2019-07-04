package com.nubeiot.core.component;

import java.nio.file.Path;
import java.util.Objects;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UnitVerticle<C extends IConfig, T extends UnitContext> extends AbstractVerticle
    implements Unit<C, T> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    private final T unitContext;
    protected C config;
    @Getter(value = AccessLevel.PROTECTED)
    private String sharedKey;
    private Path testDir;

    /**
     * For test independent
     */
    protected void injectTest(String sharedKey, Path testDir) {
        this.registerSharedKey(Strings.isBlank(sharedKey) ? toString() : sharedKey);
        this.testDir = Objects.isNull(testDir) ? FileUtils.DEFAULT_DATADIR : testDir;
    }

    @Override
    public void start() {
        logger.debug("Computing component configure from {} of {}", configFile(), configClass());
        this.config = computeConfig(config());
        logger.debug("Unit Configuration: {}", config.toJson().encode());
        this.initTestSharedData(testDir);
    }

    @Override
    public final T getContext() {
        return unitContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <U extends Unit<C, T>> U registerSharedKey(String sharedKey) {
        logger.debug("Register SharedData with shared key: {}", sharedKey);
        this.sharedKey = sharedKey;
        return (U) this;
    }

    @Override
    public final <R> R getSharedData(String dataKey, R fallback) {
        logger.debug("Retrieve SharedData by SharedKey {}", sharedKey);
        final R dataValue = SharedDataDelegate.getLocalDataValue(vertx, sharedKey, dataKey);
        return Objects.isNull(dataValue) ? fallback : dataValue;
    }

    private void initTestSharedData(Path testDir) {
        if (Objects.isNull(testDir)) {
            return;
        }
        SharedDataDelegate.addLocalDataValue(vertx, sharedKey, SharedDataDelegate.SHARED_DATADIR, testDir.toString());
        SharedDataDelegate.addLocalDataValue(vertx, sharedKey, SharedDataDelegate.SHARED_EVENTBUS,
                                             new DefaultEventController(vertx));
    }

}
