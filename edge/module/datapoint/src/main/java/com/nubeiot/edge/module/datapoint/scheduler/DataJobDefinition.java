package com.nubeiot.edge.module.datapoint.scheduler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.sql.type.Label;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface DataJobDefinition extends EnumType {

    static List<DataJobDefinition> def() {
        return AbstractDataJobDefinition.def().collect(Collectors.toList());
    }

    @NonNull
    @JsonCreator
    static DataJobDefinition create(@NonNull Map<String, Object> data) {
        String type = Strings.requireNotBlank(data.get("type"), "Data job type is missing");
        DataJobDefinition definition = AbstractDataJobDefinition.def()
                                                                .filter(o -> o.type().equals(type))
                                                                .findFirst()
                                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                    "Not supported data job type " + type));
        return ((AbstractDataJobDefinition) definition).wrap(data);
    }

    @JsonProperty("enabled")
    boolean isEnabled();

    @JsonProperty("label")
    Label getLabel();

    @JsonProperty("trigger")
    JsonObject getTrigger();

    @NonNull JsonObject toSchedule(@NonNull JsonObject config);

}
