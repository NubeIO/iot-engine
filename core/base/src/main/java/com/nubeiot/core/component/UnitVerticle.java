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

    @Override
    public final Unit<C, T> registerSharedKey(String sharedKey) {
        logger.debug("Register SharedData with shared key: {}", sharedKey);
        this.sharedKey = sharedKey;
        return this;
    }

    @Override
    public final <D> D getSharedData(String dataKey, D fallback) {
        final D dataValue = SharedDataDelegate.getLocalDataValue(vertx, sharedKey, dataKey);
        return Objects.isNull(dataValue) ? fallback : dataValue;
    }

    @Override
    public final <D> D addSharedData(String dataKey, D data) {
        SharedDataDelegate.addLocalDataValue(vertx, sharedKey, dataKey, data);
        return data;
    }

    private void initTestSharedData(Path testDir) {
        if (Objects.isNull(testDir)) {
            return;
        }
        addSharedData(SharedDataDelegate.SHARED_DATADIR, testDir.toString());
        addSharedData(SharedDataDelegate.SHARED_EVENTBUS, new DefaultEventController(vertx));
    }

}
