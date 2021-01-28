package com.nubeiot.iotdata.property;

import java.util.Optional;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.utils.Functions;
import io.reactivex.annotations.Nullable;
import io.vertx.core.json.JsonObject;

import com.nubeiot.iotdata.TimeseriesData;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents for Point value.
 *
 * @since 1.0.0
 */
@Getter
@Builder
@Jacksonized
@FieldNameConstants
public final class PointValue implements JsonData, TimeseriesData {

    private final int priority;
    private final String value;
    private final Double rawValue;

    public static PointValue createDef() {
        return PointValue.builder().priority(PointPriorityValue.DEFAULT_PRIORITY).build();
    }

    @Nullable
    public static PointValue from(@NonNull JsonObject data) {
        if (data.containsKey(Fields.priority) || data.containsKey(Fields.rawValue) || data.containsKey(Fields.value)) {
            return PointValue.builder()
                             .priority(data.getInteger(Fields.priority))
                             .rawValue(data.getDouble(Fields.rawValue))
                             .value(data.getString(Fields.value))
                             .build();
        }
        return null;
    }

    static class PointValueBuilder {

        private Integer priority;

        public PointValueBuilder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public PointValue build() {
            int p = PointPriorityValue.validateAndGet(priority);
            String v = Optional.ofNullable(this.value)
                               .orElse(Optional.ofNullable(rawValue).map(Object::toString).orElse(null));
            Double r = Optional.ofNullable(rawValue)
                               .orElse(Optional.ofNullable(this.value)
                                               .map(x -> Functions.getOrDefault((Double) null, () -> Double.valueOf(x)))
                                               .orElse(null));
            return new PointValue(p, v, r);
        }

    }

}
