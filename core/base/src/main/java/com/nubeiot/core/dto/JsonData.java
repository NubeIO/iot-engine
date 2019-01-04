package com.nubeiot.core.dto;

import java.util.Map;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;

import lombok.NonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface JsonData {

    default JsonObject toJson() {
        return toJson(mapper());
    }

    @SuppressWarnings("unchecked")
    default JsonObject toJson(ObjectMapper mapper) {
        return new JsonObject((Map<String, Object>) mapper.convertValue(this, Map.class));
    }

    default ObjectMapper mapper() {
        return Json.mapper;
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz) {
        return from(object, clazz, "Invalid data format");
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz, String errorMsg) {
        return from(object, clazz, Json.mapper, errorMsg);
    }

    @SuppressWarnings("unchecked")
    static <T extends JsonData> T from(@NonNull Object object, @NonNull Class<T> clazz, @NonNull ObjectMapper mapper,
                                       String errorMsg) {
        try {
            JsonObject entries = object instanceof String
                                 ? new JsonObject((String) object)
                                 : new JsonObject((Map<String, Object>) mapper.convertValue(object, Map.class));
            ;
            return entries.mapTo(clazz);
        } catch (IllegalArgumentException | NullPointerException | DecodeException ex) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, errorMsg, new HiddenException(ex));
        }
    }

}
