package com.nubeio.iotdata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BooleanDataType extends NumberDataType {

    private final Map<Double, List<String>> possibleValues = new HashMap<>();

    BooleanDataType() {
        super("BOOL", null);
        possibleValues.put(0.5d, Arrays.asList("true", "on", "start", "1"));
        possibleValues.put(0d, Arrays.asList("false", "off", "stop", "0", "null"));
    }

    @Override
    public Double parse(Object data) {
        if (Objects.isNull(data)) {
            return 0d;
        }
        if (data instanceof Number) {
            return ((Number) data).doubleValue() > 0.5 ? 1d : 0d;
        }
        if (data instanceof String) {
            return possibleValues.get(0.5d).contains(data) ? 1d : 0d;
        }
        return 0d;
    }

    @Override
    public String display(Double value) {
        return value > 0.5 ? "true" : "false";
    }

    @Override
    public Map<Double, List<String>> possibleValue() {
        return possibleValues;
    }

}
