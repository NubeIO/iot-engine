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
public class PointKind implements EnumType {

    public static final PointKind INPUT = new PointKind("INPUT");
    public static final PointKind OUTPUT = new PointKind("OUTPUT");
    public static final PointKind UNKNOWN = new PointKind("UNKNOWN");

    private final String type;

    public static PointKind def() { return UNKNOWN; }

    @JsonCreator
    public static PointKind factory(String name) {
        String n = Strings.optimizeMultipleSpace(name).toUpperCase(Locale.ENGLISH);
        if (UNKNOWN.type.equals(n)) {
            return UNKNOWN;
        }
        if (INPUT.type.equals(n)) {
            return INPUT;
        }
        if (OUTPUT.type.equals(n)) {
            return OUTPUT;
        }
        return new PointKind(n);
    }

    @Override
    public String type() {
        return type;
    }

}
