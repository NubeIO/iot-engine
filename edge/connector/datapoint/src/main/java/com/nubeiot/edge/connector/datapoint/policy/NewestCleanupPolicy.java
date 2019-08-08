package com.nubeiot.edge.connector.datapoint.policy;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.edge.connector.datapoint.policy.CleanupPolicy.AbstractCleanupPolicy;
import com.nubeiot.edge.connector.datapoint.policy.CleanupPolicy.GroupCleanupPolicy;
import com.nubeiot.edge.connector.datapoint.policy.CleanupPolicy.TimeCleanupPolicy;

import lombok.Getter;

@Getter
public final class NewestCleanupPolicy extends AbstractCleanupPolicy
    implements CleanupPolicy, GroupCleanupPolicy, TimeCleanupPolicy {

    public static final String TYPE = "newest";
    private final String groupBy;
    private final Duration duration;

    @JsonCreator
    public NewestCleanupPolicy(@JsonProperty("max_item") int maxItem, @JsonProperty("group_by") String groupBy,
                               @JsonProperty("duration") Duration duration) {
        super(TYPE, maxItem);
        this.groupBy = groupBy;
        this.duration = duration;
    }

}
