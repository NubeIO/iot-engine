package com.nubeiot.iotdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.PlainType;

public final class HistorySettingType extends AbstractEnumType implements PlainType {

    public static final HistorySettingType COV = new HistorySettingType("COV");
    public static final HistorySettingType PERIOD = new HistorySettingType("PERIOD", "PERIODIC");

    private HistorySettingType(String type) { super(type); }

    private HistorySettingType(String type, String... aliases) {
        super(type, aliases);
    }

    public static HistorySettingType def() { return PERIOD; }

    @JsonCreator
    public static HistorySettingType factory(String type) {
        return EnumType.factory(type, HistorySettingType.class, def());
    }

}
