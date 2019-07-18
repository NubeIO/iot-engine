package com.nubeiot.iotdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;

public final class PointCategory extends AbstractEnumType {

    public static final PointCategory GPIO = new PointCategory("GPIO");
    public static final PointCategory BACNET = new PointCategory("BACNET");
    public static final PointCategory HAYSTACK = new PointCategory("HAYSTACK");
    public static final PointCategory MODBUS = new PointCategory("MODBUS");
    public static final PointCategory UNKNOWN = new PointCategory("UNKNOWN");

    private PointCategory(String type) { super(type); }

    public static PointCategory def()  { return UNKNOWN; }

    @JsonCreator
    public static PointCategory factory(String name) {
        return EnumType.factory(name, PointCategory.class, def());
    }

}
