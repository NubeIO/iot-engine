package com.nubeiot.core.sql;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.JsonData;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Wrapper Pojo with exclude {@code null} value.
 *
 * @param <T> {@link VertxPojo}
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonPojo<T extends VertxPojo> implements JsonData {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPojo.class);
    private static final ObjectMapper MAPPER = JsonData.MAPPER.copy().setSerializationInclusion(Include.NON_NULL);
    @Getter
    @JsonIgnore
    private final T pojo;
    @Getter
    private ObjectMapper mapper = MAPPER;

    public static <T extends VertxPojo> JsonPojo<T> from(@NonNull T pojo) {
        return new JsonPojo<>(pojo);
    }

    public static <T extends VertxPojo> JsonPojo<T> from(@NonNull T pojo, @NonNull ObjectMapper mapper) {
        return new JsonPojo<>(pojo, mapper);
    }

    public static <T extends VertxPojo> JsonObject merge(@NonNull T from, @NonNull T to) {
        return from.toJson().mergeIn(new JsonPojo<>(to).toJson(), true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonObject toJson() {
        JsonObject json = this.pojo.toJson();
        try {
            return new JsonObject(mapper.readValue(mapper.writeValueAsBytes(json.getMap()), Map.class));
        } catch (IOException e) {
            LOGGER.warn("Cannot reparse pojo {}", e, pojo.getClass());
            return json;
        }
    }

    @Override
    public JsonObject toJson(Set<String> ignoreFields) {
        return toJson(mapper.copy(), ignoreFields);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonObject toJson(ObjectMapper mapper, Set<String> ignoreFields) {
        JsonObject json = this.pojo.toJson();
        try {
            return new JsonObject(mapper.readValue(mapper.addMixIn(Object.class, PropertyFilterMixIn.class)
                                                         .writer(JsonData.ignoreFields(ignoreFields))
                                                         .writeValueAsBytes(json.getMap()), Map.class));
        } catch (IOException e) {
            LOGGER.warn("Cannot reparse pojo {}", e, pojo.getClass());
            return json;
        }
    }

}
