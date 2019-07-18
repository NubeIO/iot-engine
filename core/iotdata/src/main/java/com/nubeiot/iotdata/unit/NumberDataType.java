package com.nubeiot.iotdata.unit;

import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NumberDataType implements DataType {

    @Getter
    private final String type;
    private final String unit;

    NumberDataType() {
        this("number", null);
    }

    @Override
    public String type() { return type; }

    @Override
    public final String unit() { return unit; }

    @Override
    public String display(Double value) {
        if (Strings.isBlank(unit)) {
            return String.valueOf(value);
        }
        return value + " " + unit;
    }

}
