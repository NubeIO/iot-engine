package com.nubeiot.iotdata.unit;

import com.nubeiot.iotdata.unit.DataTypeCategory.Base;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NumberDataType implements InternalDataType {

    @Getter
    @Include
    private final String type;
    private final String unit;
    private String category;
    private UnitLabel display;

    NumberDataType() {
        this("number", null);
    }

    NumberDataType(String type, String unit) {
        this(type, unit, Base.TYPE, null);
    }

    NumberDataType(String type, String unit, String category) {
        this(type, unit, category, null);
    }

    NumberDataType(DataType dt) {
        this.type = dt.type();
        this.unit = dt.unit();
        this.category = dt.category();
        this.display = dt.label();
    }

    @Override
    public String type() { return type; }

    @Override
    public final String unit() { return unit; }

    @Override
    public @NonNull String category() { return category; }

    @Override
    public UnitLabel label() { return display; }

    @Override
    public DataType setLabel(UnitLabel label) {
        this.display = label;
        return this;
    }

    @Override
    public InternalDataType setCategory(String category) {
        this.category = category;
        return this;
    }

}
