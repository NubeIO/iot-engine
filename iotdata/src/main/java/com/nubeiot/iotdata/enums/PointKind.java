package com.nubeiot.iotdata.enums;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.EnumType.AbstractEnumType;
import io.github.zero88.qwe.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.iotdata.IoTProperty.IoTChunkProperty;

public final class PointKind extends AbstractEnumType implements PlainType, IoTChunkProperty {

    public static final PointKind UNKNOWN = new PointKind("UNKNOWN");

    private PointKind(String type) {
        super(type);
    }

    public static PointKind def() { return UNKNOWN; }

    @JsonCreator
    public static PointKind factory(String name) {
        return EnumType.factory(name, PointKind.class, def());
    }

}
