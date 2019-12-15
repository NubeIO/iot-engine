package com.nubeiot.iotdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.PlainType;

/**
 * Represents {@code semantic protocol} that stands for {@code communication protocol} between {@code NubeIO Edge} and
 * actual {@code device}/{@code equipment}
 */
public final class Protocol extends AbstractEnumType implements PlainType {

    public static final Protocol GPIO = new Protocol("GPIO");
    public static final Protocol BACNET = new Protocol("BACNET");
    public static final Protocol MODBUS = new Protocol("MODBUS");
    public static final Protocol UNKNOWN = new Protocol("UNKNOWN");

    private Protocol(String type) { super(type); }

    public static Protocol def()  { return UNKNOWN; }

    @JsonCreator
    public static Protocol factory(String name) {
        return EnumType.factory(name, Protocol.class, def());
    }

}
