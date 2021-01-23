package com.nubeiot.edge.module.datapoint.scheduler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.zero88.utils.Reflections.ReflectionClass;
import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.type.Label;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;

import lombok.NonNull;

public interface DataJobDefinition extends EnumType {

    ObjectMapper MAPPER = JsonData.MAPPER.copy();

    static List<JsonObject> def() {
        return AbstractDataJobDefinition.def().map(JsonData::toJson).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    static Class<DataJobDefinition> find(@NonNull String type) {
        return (Class<DataJobDefinition>) AbstractDataJobDefinition.def()
                                                                   .filter(o -> o.type().equals(type))
                                                                   .findFirst()
                                                                   .orElseThrow(() -> new IllegalArgumentException(
                                                                       "Not supported data job type " + type))
                                                                   .getClass();
    }

    @NonNull
    @JsonCreator
    static DataJobDefinition create(
        @JacksonInject(value = DataCacheInitializer.JOB_CONFIG_CACHE) ClassGraphCache<String, DataJobDefinition> cache,
        @NonNull Map<String, Object> data) {
        String type = Strings.requireNotBlank(data.get("type"), "Data job type is missing");
        Class<? extends DataJobDefinition> clazz = Objects.isNull(cache) ? find(type) : cache.get(type);
        return ((AbstractDataJobDefinition) ReflectionClass.createObject(clazz)).wrap(data);
    }

    @JsonProperty("enabled")
    boolean isEnabled();

    @JsonProperty("label")
    Label getLabel();

    @JsonProperty("trigger")
    JsonObject getTrigger();

    @NonNull JsonObject toSchedule(@NonNull JsonObject config);

    @Override
    default ObjectMapper getMapper() {
        return MAPPER;
    }

}
