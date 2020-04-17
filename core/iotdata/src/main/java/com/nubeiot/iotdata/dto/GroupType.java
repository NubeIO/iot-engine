package com.nubeiot.iotdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.PlainType;

public final class GroupType extends AbstractEnumType implements PlainType, IoTNotion {

    public static final GroupType FOLDER = new GroupType("FOLDER");
    public static final GroupType DEVICE = new GroupType("DEVICE");
    public static final GroupType POINT = new GroupType("POINT");

    private GroupType(String type) {
        super(type);
    }

    @JsonCreator
    public static GroupType factory(String type) {
        return EnumType.factory(type, GroupType.class);
    }

}
