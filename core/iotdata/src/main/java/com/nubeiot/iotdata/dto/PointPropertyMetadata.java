package com.nubeiot.iotdata.dto;

import com.nubeiot.iotdata.dto.IoTNotion.IoTChunkNotion;
import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.iotdata.unit.UnitAlias;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
public class PointPropertyMetadata implements IoTChunkNotion {

    private final PointType pointType;
    private final PointKind pointKind;
    private final ThingType thingType;
    private final ThingCategory thingCategory;
    private final DataType unit;
    private final UnitAlias unitAlias;

}
