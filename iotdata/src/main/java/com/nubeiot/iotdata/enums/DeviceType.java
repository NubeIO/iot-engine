package com.nubeiot.iotdata.enums;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.EnumType.AbstractEnumType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.iotdata.IoTEnum;

public final class DeviceType extends AbstractEnumType implements IoTEnum {

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
        return EnumType.factory(type, DeviceType.class, MACHINE);
    }

}
