package com.nubeiot.iotdata.unit;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

@JsonInclude(Include.NON_EMPTY)
@JsonSerialize(as = DataType.class)
public interface DataType extends EnumType, Cloneable {

    DataType NUMBER = new NumberDataType();
    DataType PERCENTAGE = new NumberDataType("percentage", "%");
    DataType VOLTAGE = new NumberDataType("voltage", "V");
    DataType CELSIUS = new NumberDataType("celsius", "U+2103");
    DataType BOOLEAN = new BooleanDataType();
    DataType DBM = new NumberDataType("dBm", "dBm");
    DataType HPA = new NumberDataType("hPa", "hPa");
    DataType LUX = new NumberDataType("lux", "lx");
    DataType KWH = new NumberDataType("kWh", "kWh");
    String SEP = "::";

    @NonNull
    static DataType def() { return NUMBER; }

    @NonNull
    static DataType factory(String dbValue) {
        if (Strings.isBlank(dbValue)) {
            return def();
        }
        final String[] split = dbValue.split(SEP);
        return factory(split[0], split.length > 1 ? split[1] : null, null);
    }

    @NonNull
    @JsonCreator
    static DataType factory(@JsonProperty(value = "type") String type, @JsonProperty(value = "symbol") String unit,
                            @JsonProperty(value = "possible_values") Map<Double, List<String>> possibleValues) {
        return new NumberDataType(ReflectionField.streamConstants(DataType.class, InternalDataType.class)
                                                 .filter(t -> t.type().equalsIgnoreCase(type))
                                                 .findAny()
                                                 .orElseGet(() -> new NumberDataType(type, unit))).setPossibleValues(
            possibleValues);
    }

    @NonNull
    @JsonProperty(value = "symbol")
    String unit();

    @JsonProperty(value = "possible_values")
    Map<Double, List<String>> possibleValues();

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

    default @NonNull String display(Double value) {
        if (Strings.isBlank(unit())) {
            return String.valueOf(value);
        }
        return value + " " + unit();
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

    default Collection<String> aliases() { return null; }

}
