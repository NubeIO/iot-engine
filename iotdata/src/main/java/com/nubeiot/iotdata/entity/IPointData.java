package com.nubeiot.iotdata.entity;

import com.nubeiot.iotdata.IoTEntity;
import com.nubeiot.iotdata.TimeseriesData;
import com.nubeiot.iotdata.property.PointPriorityValue;
import com.nubeiot.iotdata.property.PointValue;

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
