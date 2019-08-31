package com.nubeiot.edge.module.datapoint.sync;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;

import lombok.NonNull;

public final class SyncServiceFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(SyncServiceFactory.class);

    public static EntityPostService get(@NonNull Vertx vertx, JsonObject syncConfig) {
        final DataSyncConfig sync = IConfig.from(syncConfig, DataSyncConfig.class);
        if (!sync.isEnabled()) {
            return EntityPostService.EMPTY;
        }
        if (DittoHttpSync.TYPE.equalsIgnoreCase(sync.getType())) {
            return new DittoHttpSync(vertx, sync.getClientConfig(), sync.getCredential());
        }
        LOGGER.warn("Not yet supported sync service type {}", sync.getType());
        return EntityPostService.EMPTY;
    }

}
