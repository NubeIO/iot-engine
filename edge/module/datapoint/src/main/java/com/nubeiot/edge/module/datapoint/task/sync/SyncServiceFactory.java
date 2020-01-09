package com.nubeiot.edge.module.datapoint.task.sync;

import java.util.Optional;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.edge.connector.ditto.DittoInitialSyncTask;
import com.nubeiot.edge.connector.ditto.DittoSyncTask;
import com.nubeiot.edge.connector.ditto.DittoTaskContext;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.sync.InitialSyncTask;
import com.nubeiot.edge.module.datapoint.sync.SyncTask;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

//TODO https://github.com/NubeIO/iot-engine/issues/140
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SyncServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncServiceFactory.class);

    public static SyncTask get(@NonNull EntityHandler handler, @NonNull JsonObject syncConfig) {
        final DataSyncConfig sync = IConfig.from(syncConfig, DataSyncConfig.class);
        if (DittoTaskContext.TYPE.equalsIgnoreCase(sync.getType())) {
            return new DittoSyncTask(new DittoTaskContext(handler, sync.getClientConfig(), sync.getCredential()));
        }
        LOGGER.warn("Not yet supported sync service type {}", sync.getType());
        return null;
    }

    public static Optional<InitialSyncTask> getInitialTask(@NonNull EntitySyncHandler syncHandler,
                                                           JsonObject syncConfig) {
        final DataSyncConfig sync = IConfig.from(syncConfig, DataSyncConfig.class);
        if (DittoTaskContext.TYPE.equalsIgnoreCase(sync.getType())) {
            return Optional.of(new DittoInitialSyncTask(
                new DittoTaskContext(syncHandler, sync.getClientConfig(), sync.getCredential())));
        }
        LOGGER.warn("Not yet supported sync service type {}", sync.getType());
        return Optional.empty();
    }

}
