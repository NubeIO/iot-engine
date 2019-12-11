package com.nubeiot.iotdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.PlainType;

public final class ThingCategory extends AbstractEnumType implements PlainType, IoTNotion {

    public static final ThingCategory TEMP = new ThingCategory("TEMP");
    public static final ThingCategory HUMIDITY = new ThingCategory("HUMIDITY");
    public static final ThingCategory MOTION = new ThingCategory("MOTION");
    public static final ThingCategory VELOCITY = new ThingCategory("VELOCITY");
    public static final ThingCategory SWITCH = new ThingCategory("SWITCH");
    public static final ThingCategory RELAY = new ThingCategory("RELAY");

    private ThingCategory(String type) {
        super(type);
    }

    @JsonCreator
    public static ThingCategory factory(String name) {
        return EnumType.factory(name, ThingCategory.class);
    }

}
