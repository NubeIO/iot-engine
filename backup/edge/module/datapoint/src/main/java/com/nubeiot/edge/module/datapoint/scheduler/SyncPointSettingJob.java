package com.nubeiot.edge.module.datapoint.scheduler;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.type.Label;

import lombok.NonNull;

public final class SyncPointSettingJob extends AbstractDataJobDefinition {

    protected SyncPointSettingJob() {
        super("SYNC_POINT_SETTING", Label.builder().label("Sync point setting data to cloud").build());
    }

    @Override
    public JsonObject toSchedule(@NonNull JsonObject config) {
        return new JsonObject();
    }

}
