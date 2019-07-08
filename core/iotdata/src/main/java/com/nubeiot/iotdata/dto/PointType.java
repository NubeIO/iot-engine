package com.nubeiot.iotdata.dto;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointType implements EnumType {

    public static final PointType DIGITAL = new PointType("DIGITAL");
    public static final PointType DC_10 = new PointType("0-10DC");
    public static final PointType DC_12 = new PointType("0-12DC");
    public static final PointType MA_20 = new PointType("4-20MA");
    public static final PointType THERMISTOR_10K = new PointType("10K-THERMISTOR");
    public static final PointType UNKNOWN = new PointType("UNKNOWN");

    private final String type;

    public static PointType def() {
        return UNKNOWN;
    }

    @JsonCreator
    public static PointType factory(String type) {
        String t = Strings.optimizeMultipleSpace(type).toUpperCase(Locale.ENGLISH);
        if (UNKNOWN.type.equals(t)) {
            return UNKNOWN;
        }
        if (DIGITAL.type.equals(t)) {
            return DIGITAL;
        }
        if (DC_10.type.equals(t)) {
            return DC_10;
        }
        if (DC_12.type.equals(t)) {
            return DC_12;
        }
        if (MA_20.type.equals(t)) {
            return MA_20;
        }
        if (THERMISTOR_10K.type.equals(t) || "10k thermistor".equals(t)) {
            return THERMISTOR_10K;
        }
        return new PointType(t);
    }

    @Override
    public String type() {
        return type;
    }

}
