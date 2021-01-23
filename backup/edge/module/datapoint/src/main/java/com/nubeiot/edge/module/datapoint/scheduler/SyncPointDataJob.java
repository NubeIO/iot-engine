package com.nubeiot.edge.module.datapoint.scheduler;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.type.Label;

public final class SyncPointDataJob extends AbstractDataJobDefinition implements DataJobDefinition {

    public SyncPointDataJob() {
        super("SYNC_POINT_DATA", Label.builder().label("Sync point data to cloud").build());
    }

    @Override
    public JsonObject toSchedule(JsonObject config) {
        return new JsonObject();
    }

}
