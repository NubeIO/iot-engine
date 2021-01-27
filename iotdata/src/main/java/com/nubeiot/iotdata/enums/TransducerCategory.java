package com.nubeiot.iotdata.enums;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.EnumType.AbstractEnumType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.iotdata.IoTEnum;

public final class TransducerCategory extends AbstractEnumType implements IoTEnum {

    public static final TransducerCategory TEMP = new TransducerCategory("TEMP");
    public static final TransducerCategory HUMIDITY = new TransducerCategory("HUMIDITY");
    public static final TransducerCategory MOTION = new TransducerCategory("MOTION");
    public static final TransducerCategory VELOCITY = new TransducerCategory("VELOCITY");
    public static final TransducerCategory SWITCH = new TransducerCategory("SWITCH");
    public static final TransducerCategory RELAY = new TransducerCategory("RELAY");

    private TransducerCategory(String type) {
        super(type);
    }

    @JsonCreator
    public static TransducerCategory factory(String name) {
        return EnumType.factory(name, TransducerCategory.class);
    }

}
