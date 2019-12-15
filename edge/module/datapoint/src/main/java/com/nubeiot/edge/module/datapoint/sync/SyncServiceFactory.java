package com.nubeiot.edge.module.datapoint.sync;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SyncServiceFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(SyncServiceFactory.class);

    public static EntityPostService get(@NonNull Vertx vertx, DataSyncConfig syncConfig) {
        if (!syncConfig.isEnabled()) {
            return EntityPostService.EMPTY;
        }
        if (AbstractDittoHttpSync.TYPE.equalsIgnoreCase(syncConfig.getType())) {
            return new DittoHttpSync(vertx, syncConfig.getClientConfig(), syncConfig.getCredential());
        }
        LOGGER.warn("Not yet supported sync service type {}", syncConfig.getType());
        return EntityPostService.EMPTY;
    }

    public static InitialSync getInitialSync(@NonNull EntitySyncHandler syncHandler, JsonObject syncConfig) {
        final DataSyncConfig sync = IConfig.from(syncConfig, DataSyncConfig.class);
        if (!sync.isEnabled()) {
            return InitialSync.EMPTY;
        }
        if (AbstractDittoHttpSync.TYPE.equalsIgnoreCase(sync.getType())) {
            return new DittoInitialSync(syncHandler, sync.getClientConfig(), sync.getCredential());
        }
        LOGGER.warn("Not yet supported sync service type {}", sync.getType());
        return InitialSync.EMPTY;
    }

}
