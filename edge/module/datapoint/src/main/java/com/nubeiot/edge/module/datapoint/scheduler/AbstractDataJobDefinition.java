package com.nubeiot.edge.module.datapoint.scheduler;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.type.Label;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.Getter;
import lombok.NonNull;

@Getter
abstract class AbstractDataJobDefinition extends AbstractEnumType implements DataJobDefinition {

    private boolean enabled = false;
    private Label label;
    private JsonObject trigger;

    AbstractDataJobDefinition(String type, Label label) {
        super(type);
        this.label = label;
        this.trigger = new JsonObject().put("type", "CRON")
                                       .put("expression", "0 0 0 1/1 * ? *")
                                       .put("timezone", "Australia/Sydney");
    }

    static Stream<DataJobDefinition> def() {
        return ReflectionClass.stream(DataJobDefinition.class.getPackage().getName(), DataJobDefinition.class,
                                      ReflectionClass.publicClass()).map(ReflectionClass::createObject);
    }

    DataJobDefinition wrap(@NonNull Map<String, Object> data) {
        this.enabled = Optional.ofNullable(data.get("enabled")).map(o -> Boolean.valueOf(o.toString())).orElse(enabled);
        this.label = Optional.ofNullable(data.get("label"))
                             .flatMap(o -> Functions.getIfThrow(() -> JsonData.from(o, Label.class)))
                             .orElse(label);
        this.trigger = Optional.ofNullable(data.get("trigger"))
                               .filter(o -> o instanceof JsonObject || o instanceof Map)
                               .map(o -> o instanceof JsonObject ? (JsonObject) o : JsonData.tryParse(o).toJson())
                               .orElse(trigger);
        return this;
    }

}
