package com.nubeiot.iotdata.dto;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.EnumType.AbstractEnumType;
import io.github.zero88.qwe.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.iotdata.dto.IoTNotion.IoTChunkNotion;

public final class PointType extends AbstractEnumType implements PlainType, IoTChunkNotion {

    public static final PointType ANALOG = new PointType("ANALOG");
    public static final PointType DIGITAL = new PointType("DIGITAL");
    public static final PointType DC_10 = new PointType("0-10DC");
    public static final PointType DC_12 = new PointType("0-12DC");
    public static final PointType MA_20 = new PointType("4-20MA");
    public static final PointType THERMISTOR_10K = new PointType("10K-THERMISTOR", "10k thermistor");
    public static final PointType UNKNOWN = new PointType("UNKNOWN");

    private PointType(String type)                    { super(type); }

    private PointType(String type, String... aliases) { super(type, aliases); }

    public static PointType def() {
        return UNKNOWN;
    }

    @JsonCreator
    public static PointType factory(String type) {
        return EnumType.factory(type, PointType.class, def());
    }

}
