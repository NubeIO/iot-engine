package com.nubeiot.edge.connector.datapoint;

import java.util.HashMap;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.iotdata.model.tables.interfaces.IMeasureUnit;
import com.nubeiot.iotdata.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.unit.DataType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class DataPointConfig implements IConfig {

    private LowdbMigration lowdbMigration = new LowdbMigration();
    @JsonProperty(PublisherConfig.NAME)
    private PublisherConfig publisherConfig;
    private BuiltinData builtinData;

    static DataPointConfig def() {
        return new DataPointConfig(new LowdbMigration(), new PublisherConfig(), BuiltinData.def());
    }

    @Override
    public String name() {
        return "__datapoint__";
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    public static final class LowdbMigration implements Shareable {

        private boolean enabled = false;
        private String path;

    }


    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    public static final class PublisherConfig extends AbstractEnumType implements IConfig {

        static final String NAME = "__publisher__";

        private boolean enabled = false;
        private JsonObject location;

        PublisherConfig() {
            super("");
        }

        @JsonCreator
        PublisherConfig(@JsonProperty("type") String type, @JsonProperty("location") JsonObject location) {
            super(type);
            this.location = location;
        }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() {
            return DataPointConfig.class;
        }

    }


    @Getter
    public static final class BuiltinData extends HashMap<String, Object> implements JsonData {

        public static BuiltinData def() {
            final BuiltinData builtinData = new BuiltinData();
            builtinData.put("measure_unit", ReflectionField.streamConstants(DataType.class, DataType.class)
                                                           .map(dt -> new MeasureUnit(dt.toJson()))
                                                           .map(IMeasureUnit::toJson)
                                                           .collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
            return builtinData;
        }

    }

}
