package com.nubeiot.core.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface JsonData {

    ObjectMapper MAPPER = Json.mapper.copy().registerModule(Deserializer.SIMPLE_MODULE);

    static <T extends JsonData> T from(Object object, Class<T> clazz) {
        return from(object, clazz, "Invalid data format");
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz, String errorMsg) {
        return from(object, clazz, Json.mapper, errorMsg);
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz, ObjectMapper mapper) {
        return from(object, clazz, mapper, null);
    }

    @SuppressWarnings("unchecked")
    static <T extends JsonData> T from(@NonNull Object object, @NonNull Class<T> clazz, @NonNull ObjectMapper mapper,
                                       String errorMsg) {
        try {
            JsonObject entries = object instanceof String
                                 ? new JsonObject((String) object)
                                 : new JsonObject((Map<String, Object>) mapper.convertValue(object, Map.class));
            return entries.mapTo(clazz);
        } catch (IllegalArgumentException | NullPointerException | DecodeException ex) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, errorMsg, new HiddenException(ex));
        }
    }

    default JsonObject toJson() {
        return toJson(mapper());
    }

    @SuppressWarnings("unchecked")
    default JsonObject toJson(ObjectMapper mapper) {
        return new JsonObject((Map<String, Object>) mapper.convertValue(this, Map.class));
    }

    default ObjectMapper mapper() { return MAPPER; }

    @NoArgsConstructor
    class DefaultJsonData extends HashMap<String, Object> implements JsonData {

        public DefaultJsonData(@NonNull Map<String, Object> map) {
            this.putAll(map);
        }

    }


    @Builder(builderClassName = "Builder")
    class SerializerFunction implements Function<Object, JsonObject> {

        private static final Logger logger = LoggerFactory.getLogger(SerializerFunction.class);

        @NonNull
        private final String backupKey;
        @NonNull
        private final ObjectMapper mapper;

        @SuppressWarnings("unchecked")
        @Override
        public JsonObject apply(Object obj) {
            if (obj instanceof JsonObject) {
                return (JsonObject) obj;
            }
            if (obj instanceof Collection) {
                return new JsonObject().put(backupKey, new JsonArray(new ArrayList((Collection) obj)));
            }
            try {
                return new JsonObject((Map<String, Object>) mapper.convertValue(obj, Map.class));
            } catch (IllegalArgumentException e) {
                logger.trace("Failed to map to json. Fallback to construct Json from plain object", e);
                return new JsonObject().put(backupKey, obj);
            }
        }

    }

}
