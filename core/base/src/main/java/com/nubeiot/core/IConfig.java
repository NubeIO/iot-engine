package com.nubeiot.core;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

public interface IConfig extends JsonData {

    @JsonIgnore
    String name();

    @JsonIgnore
    Class<? extends IConfig> parent();

    @JsonIgnore
    default boolean isRoot() {
        return Objects.isNull(parent());
    }

    @Override
    @SuppressWarnings("unchecked")
    default JsonObject toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return new JsonObject((Map<String, Object>) mapper.convertValue(this, Map.class));
    }

    static <T extends IConfig> T from(Object object, Class<T> clazz) {
        return from(object, clazz, "Invalid config format");
    }

    static <T extends IConfig> T from(Object object, Class<T> clazz, String errorMsg) {
        return from(object, clazz, errorMsg, null);
    }

    static <T extends IConfig> T from(Object object, Class<T> clazz, HiddenException cause) {
        return from(object, clazz, null, cause);
    }

    static <T extends IConfig> T from(Object object, Class<T> clazz, String errorMsg, HiddenException cause) {
        try {
            JsonObject entries = object instanceof String
                                 ? new JsonObject((String) object)
                                 : JsonObject.mapFrom(Objects.requireNonNull(object));
            return ((CreateConfig<T>) Reflections.createObject(clazz, new CreateConfig<>(clazz, entries))).get();
        } catch (IllegalArgumentException | NullPointerException | DecodeException | HiddenException ex) {
            HiddenException hidden = ex instanceof HiddenException ? (HiddenException) ex : new HiddenException(ex);
            if (Objects.nonNull(cause)) {
                hidden.addSuppressed(Objects.nonNull(cause.getCause()) ? cause.getCause() : cause);
            }
            String msg = Strings.isNotBlank(errorMsg) ? errorMsg : "Invalid config format";
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, msg, hidden);
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends IConfig> T merge(T from, T to) {
        return (T) from(from.toJson().mergeIn(to.toJson(), true), to.getClass());
    }

    @RequiredArgsConstructor
    class CreateConfig<T extends IConfig> implements BiConsumer<T, HiddenException>, Supplier<T> {

        private T object;
        private final Class<T> clazz;
        private final JsonObject entries;

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
                ObjectMapper mapper = new ObjectMapper();
                mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                      .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
                return mapper.convertValue(values.getMap(), clazz);
            } catch (IllegalArgumentException | ClassCastException e) {
                throw new HiddenException(NubeException.ErrorCode.INVALID_ARGUMENT, e);
            }
        }

        @Override
        public T get() { return object; }

    }

}
