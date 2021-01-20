package com.nubeiot.iotdata;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.EnumType.AbstractEnumType;
import io.github.zero88.qwe.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents a {@code semantic protocol} that express {@code communication protocol}
 */
public final class Protocol extends AbstractEnumType implements PlainType {

    public static final Protocol BACNET = new Protocol("BACNET");
    public static final Protocol DATAPOINT = new Protocol("DATAPOINT");
    public static final Protocol MQTT = new Protocol("MQTT");
    public static final Protocol KAFKA = new Protocol("KAFKA");
    public static final Protocol USB = new Protocol("USB");
    public static final Protocol MODBUS = new Protocol("MODBUS");
    public static final Protocol LORA = new Protocol("LORA");
    public static final Protocol UNKNOWN = new Protocol("UNKNOWN");

    private Protocol(String type) { super(type); }

    public static Protocol def()  { return UNKNOWN; }

    @JsonCreator
    public static Protocol factory(String name) {
        return EnumType.factory(name, Protocol.class, def());
    }

}
