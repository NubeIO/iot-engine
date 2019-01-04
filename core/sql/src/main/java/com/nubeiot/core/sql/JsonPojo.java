package com.nubeiot.core.sql;

import java.util.Map;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.JsonData;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonPojo<T extends VertxPojo> implements JsonData {

    @Getter
    @JsonIgnore
    private final T pojo;

    public static <T extends VertxPojo> JsonPojo<T> from(@NonNull T pojo) {
        return new JsonPojo<>(pojo);
    }

    @Override
    public ObjectMapper mapper() {
        return Json.mapper.setSerializationInclusion(Include.NON_NULL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonObject toJson() {
        return new JsonObject((Map<String, Object>) mapper().convertValue(this.pojo.toJson().getMap(), Map.class));
    }

}
