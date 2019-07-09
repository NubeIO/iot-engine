package com.nubeiot.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.NubeConfig.SecretConfig;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.core.utils.Functions.Silencer;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface IConfig extends JsonData {

    ObjectMapper MAPPER = JsonData.MAPPER.copy().setSerializationInclusion(Include.NON_NULL);
    ObjectMapper MAPPER_IGNORE_UNKNOWN_PROPERTY = MAPPER.copy()
                                                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                                                   false);

    static <T extends IConfig> T fromClasspath(String jsonFile, Class<T> clazz) {
        return IConfig.from(Configs.loadJsonConfig(jsonFile), clazz);
    }

    static <T extends IConfig> T from(Object data, Class<T> clazz) {
        return from(data, clazz, "Invalid config format");
    }

    static <T extends IConfig> T from(Object data, Class<T> clazz, String errorMsg) {
        return from(data, clazz, errorMsg, null);
    }

    static <T extends IConfig> T from(Object data, Class<T> clazz, HiddenException cause) {
        return from(data, clazz, null, cause);
    }

    static <T extends IConfig> T from(@NonNull Object data, @NonNull Class<T> clazz, String errorMsg,
                                      HiddenException cause) {
        try {
            JsonObject entries = data instanceof String
                                 ? new JsonObject((String) data)
                                 : JsonObject.mapFrom(Objects.requireNonNull(data));
            return ReflectionClass.createObject(clazz, new CreateConfig<>(clazz, entries, MAPPER)).get();
        } catch (IllegalArgumentException | NullPointerException | DecodeException | HiddenException ex) {
            HiddenException hidden = ex instanceof HiddenException ? (HiddenException) ex : new HiddenException(ex);
            if (Objects.nonNull(cause)) {
                hidden.addSuppressed(Objects.nonNull(cause.getCause()) ? cause.getCause() : cause);
            }
            String msg = Strings.isNotBlank(errorMsg) ? errorMsg : "Invalid config format";
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, msg, hidden);
        }
    }

    static <T extends IConfig> T merge(@NonNull JsonObject from, @NonNull JsonObject to, @NonNull Class<T> clazz) {
        return from(from.mergeIn(to, true), clazz);
    }

    @SuppressWarnings("unchecked")
    static <T extends IConfig> T merge(@NonNull Object from, @NonNull Object to, @NonNull Class<T> clazz) {
        if (from instanceof JsonObject && to instanceof JsonObject) {
            return merge((JsonObject) from, (JsonObject) to, clazz);
        }
        if (clazz.isInstance(from) && clazz.isInstance(to)) {
            return ((T) from).merge((T) to);
        }
        if (clazz.isInstance(from)) {
            return ((T) from).merge(from(to, clazz));
        }
        if (clazz.isInstance(to)) {
            return from(from, clazz).merge((T) to);
        }
        return from(from, clazz).merge(from(to, clazz));
    }

    static <C extends IConfig> C merge(JsonObject oldOne, JsonObject newOne, boolean isUpdated, Class<C> clazz) {
        if (Objects.isNull(newOne)) {
            return IConfig.from(oldOne, clazz);
        }
        if (isUpdated) {
            return IConfig.from(newOne, clazz);
        }
        JsonObject oldApp = IConfig.from(oldOne, clazz).toJson();
        JsonObject newApp = IConfig.from(newOne, clazz).toJson();
        return IConfig.merge(oldApp, newApp, clazz);
    }

    static <C extends IConfig> C parseConfig(JsonObject config, Class<C> clazz, Supplier<C> fallback) {
        try {
            return IConfig.from(config, clazz);
        } catch (NubeException ex) {
            return fallback.get();
        }
    }

    @JsonIgnore
    String name();

    @JsonIgnore
    Class<? extends IConfig> parent();

    @JsonIgnore
    default boolean isRoot() {
        return Objects.isNull(parent());
    }

    @SuppressWarnings("unchecked")
    default <T extends IConfig> T merge(@NonNull T to) {
        return (T) merge(toJson(), to.toJson(), getClass());
    }

    default <T extends IConfig> JsonObject mergeToJson(@NonNull T to) {
        return this.toJson().mergeIn(to.toJson(), true);
    }

    @SuppressWarnings("unchecked")
    static <C extends IConfig> C recomputeReferences(C config, SecretConfig secretConfig) {
        return IConfig.from(recomputeReferences(config.toJson(), (jObject, key, value) -> {
            jObject.put(key, secretConfig.decode(value).getValue());
        }), (Class<C>) config.getClass());
    }

    static Object recomputeReferences(Object config, TriConsumer<JsonObject, String, String> consumer) {
        if (config instanceof JsonObject) {
            JsonObject jObject = (JsonObject) config;
            Set<String> keys = jObject.fieldNames();

            for (String key : keys) {
                Object value = jObject.getValue(key);
                if (value instanceof JsonObject || value instanceof JsonArray) {
                    recomputeReferences(value, consumer);
                } else if (value instanceof String) {
                    consumer.accept(jObject, key, (String) value);
                }
            }
        } else if (config instanceof JsonArray) {
            JsonArray jArray = (JsonArray) config;
            for (Object obj : jArray) {
                recomputeReferences(obj, consumer);
            }
        }

        return config;
    }

    @Override
    @SuppressWarnings("unchecked")
    default JsonObject toJson() {
        List<? extends IConfig> fieldValues = ReflectionField.getFieldValuesByType(this, IConfig.class);
        JsonObject jsonObject = new JsonObject(mapper().convertValue(this, Map.class));
        fieldValues.forEach(val -> jsonObject.put(val.name(), val.toJson()));
        return jsonObject;
    }

    default ObjectMapper mapper() {
        return MAPPER;
    }

    @RequiredArgsConstructor
    class CreateConfig<T extends IConfig> extends Silencer<T> {

        private final Class<T> clazz;
        private final JsonObject entries;
        private final ObjectMapper mapper;

        @Override
        public void accept(T temp, HiddenException throwable) {
            if (Objects.nonNull(throwable)) {
                throw throwable;
            }
            try {
                object = create(temp.name(), entries, clazz);
            } catch (HiddenException ex) {
                if (temp.isRoot()) {
                    throw ex;
                }
                IConfig parent = from(entries, temp.parent(), ex);
                JsonObject parentValue = parent instanceof Map && ((Map) parent).containsKey(parent.name())
                                         ? parent.toJson().getJsonObject(parent.name(), new JsonObject())
                                         : parent.toJson();
                Object currentValue = parentValue.getValue(temp.name());
                if (Objects.isNull(currentValue)) {
                    throw ex;
                }
                object = from(currentValue, clazz);
            }
        }

        private T create(String name, JsonObject entries, Class<T> clazz) {
            try {
                JsonObject values = Strings.isNotBlank(name) && entries.containsKey(name)
                                    ? entries.getJsonObject(name)
                                    : entries;
                return mapper.convertValue(values.getMap(), clazz);
            } catch (IllegalArgumentException | ClassCastException e) {
                throw new HiddenException(NubeException.ErrorCode.INVALID_ARGUMENT, e);
            }
        }

    }

}
