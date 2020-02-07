package com.nubeiot.iotdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.PlainType;
import com.nubeiot.iotdata.dto.IoTNotion.IoTChunkNotion;

public final class ThingType extends AbstractEnumType implements PlainType, IoTChunkNotion {

    public static final ThingType SENSOR = new ThingType("SENSOR");
    public static final ThingType ACTUATOR = new ThingType("ACTUATOR");

    private ThingType(String type) {
        super(type);
    }

    public static ThingType def() { return SENSOR; }

    @JsonCreator
    public static ThingType factory(String name) {
        return EnumType.factory(name, ThingType.class, def());
    }

}
