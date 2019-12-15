package com.nubeiot.edge.module.datapoint.scheduler;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.type.Label;

import lombok.NonNull;

public final class SyncEdgeJob extends AbstractDataJobDefinition implements DataJobDefinition {

    public SyncEdgeJob() {
        super("SYNC_EDGE_INFO", Label.builder().label("Sync edge information to cloud").build());
    }

    @Override
    public JsonObject toSchedule(@NonNull JsonObject config) {
        return new JsonObject();
    }

}
