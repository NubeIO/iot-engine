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
public final class PointCategory implements EnumType {

    public static final PointCategory GPIO = new PointCategory("GPIO");
    public static final PointCategory BACNET = new PointCategory("BACNET");
    public static final PointCategory HAYSTACK = new PointCategory("HAYSTACK");
    public static final PointCategory MODBUS = new PointCategory("MODBUS");
    public static final PointCategory UNKNOWN = new PointCategory("UNKNOWN");

    private final String type;

    public static PointCategory def() { return UNKNOWN; }

    @JsonCreator
    public static PointCategory factory(String name) {
        String n = Strings.optimizeMultipleSpace(name).toUpperCase(Locale.ENGLISH);
        if (UNKNOWN.type.equals(n)) {
            return UNKNOWN;
        }
        if (GPIO.type.equals(n)) {
            return GPIO;
        }
        if (BACNET.type.equals(n)) {
            return BACNET;
        }
        if (HAYSTACK.type.equals(n)) {
            return HAYSTACK;
        }
        if (MODBUS.type.equals(n)) {
            return MODBUS;
        }
        return new PointCategory(n);
    }

    @Override
    public String type() {
        return type;
    }

}
