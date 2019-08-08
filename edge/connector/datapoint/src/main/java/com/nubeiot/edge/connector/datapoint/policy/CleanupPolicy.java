package com.nubeiot.edge.connector.datapoint.policy;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.EnumType;

@JsonNaming(value = SnakeCaseStrategy.class)
@JsonTypeInfo(use = Id.NAME, property = "type", visible = true)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = OldestCleanupPolicy.class, name = OldestCleanupPolicy.TYPE),
    @JsonSubTypes.Type(value = NewestCleanupPolicy.class, name = NewestCleanupPolicy.TYPE),
})
public interface CleanupPolicy extends EnumType {

    int getMaxItem();

    interface GroupCleanupPolicy extends CleanupPolicy {

        String getGroupBy();

    }


    interface TimeCleanupPolicy extends CleanupPolicy {

        Duration getDuration();

    }


    class AbstractCleanupPolicy extends AbstractEnumType implements CleanupPolicy {

        private final int maxItem;

        protected AbstractCleanupPolicy(String type, int maxItem) {
            super(type);
            this.maxItem = maxItem;
        }

        @Override
        public final int getMaxItem() {
            return maxItem;
        }

    }

}
