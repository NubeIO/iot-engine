package com.nubeiot.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface IConfig extends JsonData {

    String CONFIG_FILTER = "configFilter";

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

    @Override
    @SuppressWarnings("unchecked")
    default JsonObject toJson() {
        List<? extends IConfig> fieldValues = Reflections.findFieldValueByType(this, IConfig.class);
        JsonObject json = new JsonObject();
        Set<String> fields = new HashSet<>();
        fieldValues.forEach(val -> {
            fields.add(val.name());
            json.put(val.name(), val.toJson());
        });
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept(fields);
        mapper.setFilterProvider(new SimpleFilterProvider().addFilter(CONFIG_FILTER, filter));
        return new JsonObject(mapper.convertValue(this, Map.class)).mergeIn(json);
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
                return values.mapTo(clazz);
            } catch (IllegalArgumentException | ClassCastException e) {
                throw new HiddenException(NubeException.ErrorCode.INVALID_ARGUMENT, e);
            }
        }

        @Override
        public T get() { return object; }

    }

}
