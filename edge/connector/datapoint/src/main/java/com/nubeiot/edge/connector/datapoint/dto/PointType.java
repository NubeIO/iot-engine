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
public final class PointType implements EnumType {

    public static final PointType DIGITAL = new PointType("digital");
    public static final PointType DC_10 = new PointType("0-10dc");
    public static final PointType DC_12 = new PointType("0-12dc");
    public static final PointType MA_20 = new PointType("4-20ma");
    public static final PointType THERMISTOR_10K = new PointType("10k-thermistor");
    public static final PointType UNKNOWN = new PointType("unknown");

    private final String type;

    public static PointType def() {
        return UNKNOWN;
    }

    @JsonCreator
    public static PointType factory(String type) {
        String t = Strings.optimizeMultipleSpace(type).toLowerCase(Locale.ENGLISH);
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
