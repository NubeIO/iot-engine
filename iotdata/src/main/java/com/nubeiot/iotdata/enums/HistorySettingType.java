package com.nubeiot.iotdata.enums;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.EnumType.AbstractEnumType;
import io.github.zero88.qwe.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.iotdata.IoTProperty;

public final class HistorySettingType extends AbstractEnumType implements PlainType, IoTProperty {

    public static final HistorySettingType COV = new HistorySettingType("COV");
    public static final HistorySettingType PERIOD = new HistorySettingType("PERIOD", "PERIODIC");

    private HistorySettingType(String type) { super(type); }

    private HistorySettingType(String type, String... aliases) {
        super(type, aliases);
    }

    @JsonCreator
    public static HistorySettingType factory(String type) {
        return EnumType.factory(type, HistorySettingType.class);
    }

}
