package com.nubeiot.iotdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.PlainType;

public final class EquipType extends AbstractEnumType implements PlainType {

    public static final EquipType HVAC = new EquipType("HVAC");
    public static final EquipType DROPLET = new EquipType("DROPLET");

    private EquipType(String type) {
        super(type);
    }

    @JsonCreator
    public static EquipType factory(String type) {
        return EnumType.factory(type, EquipType.class);
    }

}
