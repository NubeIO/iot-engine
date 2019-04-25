package com.nubeiot.edge.connector.datapoint.dto;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HistorySettingType implements EnumType {

    public static final HistorySettingType COV = new HistorySettingType("COV");
    public static final HistorySettingType PERIOD = new HistorySettingType("PERIOD");

    private final String type;

    public static HistorySettingType def() { return PERIOD; }

    @JsonCreator
    public static HistorySettingType factory(String type) {
        String t = Strings.optimizeMultipleSpace(type).toUpperCase(Locale.ENGLISH);
        if (COV.type.equals(t)) {
            return COV;
        }
        if (PERIOD.type.equals(t) || "PERIODIC".equals(t)) {
            return PERIOD;
        }
        return new HistorySettingType(t);
    }

    @Override
    public String type() { return type; }

}
