package com.nubeiot.iotdata.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.iotdata.property.PointPriorityValue;
import com.nubeiot.iotdata.property.PointValue;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@FieldNameConstants
@Accessors(fluent = true)
public abstract class AbstractPointData<K> implements IPointData<K> {

    @JsonProperty(Fields.key)
    private final K key;
    @NonNull
    @JsonProperty(Fields.pointId)
    private final String pointId;
    @NonNull
    @JsonProperty(Fields.presentValue)
    private final PointValue presentValue;
    @NonNull
    @JsonProperty(Fields.priorityValue)
    private final PointPriorityValue priorityValue;

}
