package com.nubeiot.iotdata.unit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class NumberDataType implements InternalDataType {

    @Getter
    @Include
    private final String type;
    private final String unit;
    private final String category;
    private Map<Double, List<String>> possibleValues = new HashMap<>();

    NumberDataType() {
        this("number", null);
    }

    NumberDataType(String type, String unit) {
        this(type, unit, DataTypeCategory.DEFAULT);
    }

    NumberDataType(DataType dt) {
        this.type = dt.type();
        this.unit = dt.unit();
        this.category = dt.category();
        setPossibleValues(dt.possibleValues());
    }

    @Override
    public String type() { return type; }

    @Override
    public final String unit() { return unit; }

    @Override
    public @NonNull String category() { return category; }

    @Override
    public Map<Double, List<String>> possibleValues() {
        return possibleValues;
    }

    @Override
    public InternalDataType setPossibleValues(Map<Double, List<String>> possibleValues) {
        Optional.ofNullable(possibleValues).ifPresent(pv -> this.possibleValues.putAll(pv));
        return this;
    }

}
