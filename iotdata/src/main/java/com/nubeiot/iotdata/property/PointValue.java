package com.nubeiot.iotdata.property;

import io.github.zero88.qwe.dto.JsonData;
import io.vertx.core.json.JsonObject;

import com.nubeiot.iotdata.TimeseriesData;

import lombok.Getter;

/**
 * Represents for Point value.
 *
 * @since 1.0.0
 */
@Getter
public final class PointValue implements JsonData, TimeseriesData {

    private final int priority;
    private final Double value;

    /**
     * Instantiates a new Point value.
     *
     * @param priority the priority
     * @param value    the value
     * @since 1.0.0
     */
    public PointValue(int priority, Double value) {
        this.priority = PointPriorityValue.validateAndGet(priority);
        this.value = value;
    }

    /**
     * From json point value.
     *
     * @param json the json
     * @return the point value
     * @since 1.0.0
     */
    public PointValue fromJson(JsonObject json) {
        return new PointValue(json.getInteger("priority", PointPriorityValue.DEFAULT_PRIORITY),
                              json.getDouble("value"));
    }

}
