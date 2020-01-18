package com.nubeiot.iotdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.PlainType;

public final class PointKind extends AbstractEnumType implements PlainType, IoTNotion {

    public static final PointKind INPUT = new PointKind("INPUT");
    public static final PointKind OUTPUT = new PointKind("OUTPUT");
    public static final PointKind SET_POINT = new PointKind("SET_POINT");
    public static final PointKind COMMAND = new PointKind("COMMAND");
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
