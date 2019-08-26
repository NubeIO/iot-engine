package com.nubeiot.iotdata.unit;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.iotdata.unit.DataTypeCategory.Base;

import lombok.NonNull;

@JsonInclude(Include.NON_EMPTY)
@JsonSerialize(as = DataType.class)
public interface DataType extends EnumType, Cloneable {

    String SEP = "::";

    static Stream<DataType> available() {
        return ReflectionClass.stream(DataType.class.getPackage().getName(), DataTypeCategory.class,
                                      ClassInfo::isInterface)
                              .flatMap(clazz -> ReflectionField.streamConstants(clazz, InternalDataType.class));
    }

    @NonNull
    static DataType def() { return Base.NUMBER; }

    @NonNull
    static DataType factory(String dittoValue) {
        if (Strings.isBlank(dittoValue)) {
            return def();
        }
        final String[] split = dittoValue.split(SEP);
        return factory(split[0], split.length > 1 ? split[1] : null);
    }

    @NonNull
    static DataType factory(String type, String unit) {
        return factory(type, unit, Base.NUMBER.category(), null);
    }

    @NonNull
    @JsonCreator
    static DataType factory(@JsonProperty(value = "type") String type, @JsonProperty(value = "symbol") String unit,
                            @JsonProperty(value = "category") String category,
                            @JsonProperty(value = "display") Map<String, String> display) {
        final DataType dt = available().filter(t -> t.type().equalsIgnoreCase(type))
                                       .findAny()
                                       .orElseGet(() -> new NumberDataType(type, unit));
        return new NumberDataType(dt).setCategory(category).setLabel(UnitLabel.create(display));
    }

    @NonNull
    static DataType factory(@NonNull JsonObject dataType, UnitLabel label) {
        return factory(dataType.getString("type"), dataType.getString("symbol"), dataType.getString("category"),
                       null).setLabel(label);
    }

    @NonNull
    @JsonProperty(value = "symbol")
    String unit();

    @NonNull
    @JsonProperty(value = "category")
    String category();

    @JsonProperty(value = "label")
    UnitLabel label();

    DataType setLabel(UnitLabel label);

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

    default @NonNull String label(Double value) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (Objects.nonNull(label())) {
            String label = label().eval(value);
            if (Strings.isBlank(label)) {
                return label;
            }
        }
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
