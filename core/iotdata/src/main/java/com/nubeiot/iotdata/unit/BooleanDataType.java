package com.nubeiot.iotdata.unit;

import java.util.Arrays;
import java.util.Objects;

public final class BooleanDataType extends NumberDataType {

    BooleanDataType() {
        super("bool", null);
        possibleValues().put(0.5d, Arrays.asList("true", "on", "start", "1"));
        possibleValues().put(0d, Arrays.asList("false", "off", "stop", "0", "null"));
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
            return possibleValues().get(0.5d).contains(data) ? 1d : 0d;
        }
        return 0d;
    }

    @Override
    public String display(Double value) {
        return value > 0.5 ? "true" : "false";
    }

}
