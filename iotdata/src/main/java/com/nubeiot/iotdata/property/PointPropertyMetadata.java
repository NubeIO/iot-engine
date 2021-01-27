package com.nubeiot.iotdata.property;

import com.nubeiot.iotdata.IoTProperty;
import com.nubeiot.iotdata.enums.PointKind;
import com.nubeiot.iotdata.enums.PointType;
import com.nubeiot.iotdata.enums.TransducerCategory;
import com.nubeiot.iotdata.enums.TransducerType;
import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.iotdata.unit.UnitAlias;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
public class PointPropertyMetadata implements IoTProperty {

    private final PointType pointType;
    private final PointKind pointKind;
    private final TransducerType transducerType;
    private final TransducerCategory transducerCategory;
    private final DataType unit;
    private final UnitAlias unitAlias;

}
