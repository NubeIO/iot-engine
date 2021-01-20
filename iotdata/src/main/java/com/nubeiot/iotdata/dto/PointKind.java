package com.nubeiot.iotdata.dto;

import io.github.zero88.msa.bp.dto.EnumType;
import io.github.zero88.msa.bp.dto.EnumType.AbstractEnumType;
import io.github.zero88.msa.bp.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.iotdata.dto.IoTNotion.IoTChunkNotion;

public final class PointKind extends AbstractEnumType implements PlainType, IoTChunkNotion {

    public static final PointKind INPUT = new PointKind("INPUT");
    public static final PointKind MULTI_STATE_INPUT = new PointKind("MULTI_STATE_INPUT");
    public static final PointKind OUTPUT = new PointKind("OUTPUT");
    public static final PointKind MULTI_STATE_OUTPUT = new PointKind("MULTI_STATE_OUTPUT");
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
