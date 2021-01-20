package com.nubeiot.iotdata.dto;

import io.github.zero88.msa.bp.dto.EnumType;
import io.github.zero88.msa.bp.dto.EnumType.AbstractEnumType;
import io.github.zero88.msa.bp.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class DeviceType extends AbstractEnumType implements PlainType, IoTNotion {

    public static final DeviceType MACHINE = new DeviceType("MACHINE");
    public static final DeviceType GATEWAY = new DeviceType("GATEWAY");
    public static final DeviceType EQUIPMENT = new DeviceType("EQUIPMENT");
    public static final DeviceType HVAC = new DeviceType("HVAC");
    public static final DeviceType DROPLET = new DeviceType("DROPLET");

    private DeviceType(String type) {
        super(type);
    }

    @JsonCreator
    public static DeviceType factory(String type) {
        return EnumType.factory(type, DeviceType.class);
    }

}
