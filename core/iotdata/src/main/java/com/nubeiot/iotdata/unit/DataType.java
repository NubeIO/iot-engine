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
import com.nubeiot.iotdata.unit.DataTypeCategory.ElectricPotential;
import com.nubeiot.iotdata.unit.DataTypeCategory.Illumination;
import com.nubeiot.iotdata.unit.DataTypeCategory.Power;
import com.nubeiot.iotdata.unit.DataTypeCategory.Pressure;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;
import com.nubeiot.iotdata.unit.DataTypeCategory.Velocity;

import lombok.NonNull;

@JsonInclude(Include.NON_EMPTY)
@JsonSerialize(as = DataType.class)
public interface DataType extends EnumType, Cloneable {

    DataType NUMBER = new NumberDataType();
    DataType PERCENTAGE = new NumberDataType("percentage", "%");
    DataType VOLTAGE = new NumberDataType("voltage", "V", ElectricPotential.TYPE);
    DataType CELSIUS = new NumberDataType("celsius", "U+2103", Temperature.TYPE);
    DataType BOOLEAN = new BooleanDataType();
    DataType DBM = new NumberDataType("dBm", "dBm", Power.TYPE);
    DataType HPA = new NumberDataType("hPa", "hPa", Pressure.TYPE);
    DataType LUX = new NumberDataType("lux", "lx", Illumination.TYPE);
    DataType KWH = new NumberDataType("kWh", "kWh", Power.TYPE);
    DataType RPM = new NumberDataType("rpm", "rpm", Velocity.TYPE);
    String SEP = "::";

    @NonNull
    static DataType def() { return NUMBER; }

    @NonNull
    static DataType factory(String dbValue) {
        if (Strings.isBlank(dbValue)) {
            return def();
        }
        final String[] split = dbValue.split(SEP);
        return factory(split[0], split.length > 1 ? split[1] : null);
    }

    @NonNull
    static DataType factory(String type, String unit) {
        return factory(type, unit, NUMBER.category(), null);
    }

    @NonNull
    @JsonCreator
    static DataType factory(@JsonProperty(value = "type") String type, @JsonProperty(value = "symbol") String unit,
                            @JsonProperty(value = "category") String category,
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

    @NonNull
    @JsonProperty(value = "category")
    String category();

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
