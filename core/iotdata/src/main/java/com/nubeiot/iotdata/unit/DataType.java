package com.nubeiot.iotdata.unit;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface DataType extends EnumType {

    DataType NUMBER = new NumberDataType();
    DataType PERCENTAGE = new PercentageDataType();
    DataType VOLTAGE = new VoltageDataType();
    DataType CELSIUS = new CelsiusDataType();
    DataType BOOLEAN = new BooleanDataType();
    DataType DBM = new DBMDataType();
    DataType HPA = new HPADataType();
    DataType LUX = new LuxDataType();
    DataType KWH = new KWHDataType();
    String SEP = "::";

    @NonNull
    static DataType def() { return NUMBER; }

    @NonNull
    static Predicate<Field> filter(String t) {
        return f -> ReflectionField.getConstant(DataType.class, f, def()).type().equalsIgnoreCase(t);
    }

    @NonNull
    @JsonCreator
    static DataType factory(String dbValue) {
        if (Strings.isBlank(dbValue)) {
            return def();
        }
        final String[] split = dbValue.split(SEP);
        return factory(split[0], split.length > 1 ? split[1] : null);
    }

    @NonNull
    static DataType factory(String type, String unit) {
        return ReflectionField.streamConstants(DataType.class, DataType.class, filter(Strings.requireNotBlank(type)))
                              .findFirst()
                              .orElse(new NumberDataType(type, unit));
    }

    @NonNull
    default String unit() {
        return "";
    }

    default Double parse(Object data) {
        if (Objects.isNull(data)) {
            return 0d;
        }
        if (data instanceof Number) {
            return ((Number) data).doubleValue();
        }
        if (data instanceof String) {
            return Double.valueOf(((String) data).replaceAll(unit(), ""));
        }
        return 0d;
    }

    @NonNull
    default String display(Double value) {
        return Objects.isNull(value) ? "" : value.toString();
    }

    @JsonIgnore
    default Map<Double, List<String>> possibleValue() {
        return null;
    }

    /**
     * Presents persist value
     *
     * @return persist value
     */
    @JsonIgnore
    default String value() {
        return this.type() + (Strings.isBlank(this.unit()) ? "" : SEP + this.unit());
    }

}
