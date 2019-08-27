package com.nubeiot.edge.module.datapoint.scheduler;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.type.Label;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.policy.CleanupPolicy;
import com.nubeiot.edge.module.datapoint.policy.OldestCleanupPolicy;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointMetadata;

import lombok.NonNull;

public final class PurgeHistoryDataJob extends AbstractDataJobDefinition implements DataJobDefinition {

    private CleanupPolicy policy = new OldestCleanupPolicy(100, PointMetadata.INSTANCE.requestKeyName(),
                                                           Duration.ofDays(30));

    PurgeHistoryDataJob() {
        super("PURGE_HISTORY_DATA", Label.builder().label("Purge point history data").build());
    }

    @Override
    public JsonObject toSchedule(JsonObject config) {
        return new JsonObject();
    }

    @JsonProperty("policy")
    public CleanupPolicy policy() {
        return policy;
    }

    @Override
    DataJobDefinition wrap(@NonNull Map<String, Object> data) {
        super.wrap(data);
        this.policy = Optional.ofNullable(data.get("policy"))
                              .flatMap(o -> Functions.getIfThrow(() -> JsonData.from(o, CleanupPolicy.class)))
                              .orElse(policy);
        return this;
    }

}
