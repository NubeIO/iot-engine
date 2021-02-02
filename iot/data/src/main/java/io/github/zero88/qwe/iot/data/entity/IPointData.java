package io.github.zero88.qwe.iot.data.entity;

import io.github.zero88.qwe.iot.data.IoTEntity;
import io.github.zero88.qwe.iot.data.TimeseriesData;
import io.github.zero88.qwe.iot.data.property.PointPriorityValue;
import io.github.zero88.qwe.iot.data.property.PointValue;

import lombok.NonNull;

public interface IPointData<K> extends IoTEntity<K>, TimeseriesData {

    /**
     * Retrieve a point identifier that point data belongs to
     *
     * @return point identifier
     */
    @NonNull String pointId();

    /**
     * Define present value
     *
     * @return present value
     * @see PointValue
     */
    PointValue presentValue();

    /**
     * Define point priority value
     *
     * @return point priority value
     * @see PointPriorityValue
     */
    PointPriorityValue priorityValue();

}
