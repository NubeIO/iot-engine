package com.nubeiot.edge.module.datapoint.scheduler;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.type.Label;

import lombok.NonNull;

public final class SyncDeviceJob extends AbstractDataJobDefinition implements DataJobDefinition {

    public SyncDeviceJob() {
        super("SYNC_DEVICE_INFO", Label.builder().label("Sync device information to cloud").build());
    }

    @Override
    public JsonObject toSchedule(@NonNull JsonObject config) {
        return null;
    }

}
