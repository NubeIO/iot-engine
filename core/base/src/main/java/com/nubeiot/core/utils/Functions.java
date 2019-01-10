package com.nubeiot.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.exceptions.HiddenException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Functions {

    private static final Logger logger = LoggerFactory.getLogger(Reflections.class);

    @SafeVarargs
    public static <T> Predicate<T> and(Predicate<T>... predicates) {
        return Arrays.stream(predicates).reduce(Predicate::and).orElse(x -> true);
    }

    @SafeVarargs
    public static <T> Predicate<T> or(Predicate<T>... predicates) {
        return Arrays.stream(predicates).reduce(Predicate::or).orElse(x -> false);
    }

    public static class Silencer<T> implements BiConsumer<T, HiddenException>, Supplier<T> {

        protected T object;

        @Override
        public void accept(T t, HiddenException e) {
            if (Objects.nonNull(e)) {
                logger.warn("Failed to retrieve object", e);
                return;
            }
            object = t;
        }

        @Override
        public final T get() {
            return object;
        }

    }


    @Builder(builderClassName = "Builder")
    public static class JsonFunction implements Function<Object, JsonObject> {

        @NonNull
        private final String collectionKey;
        @NonNull
        private final ObjectMapper mapper;

        @SuppressWarnings("unchecked")
        @Override
        public JsonObject apply(Object obj) {
            if (obj instanceof JsonObject) {
                return (JsonObject) obj;
            }
            if (obj instanceof Collection) {
                return new JsonObject().put(collectionKey, new JsonArray(new ArrayList((Collection) obj)));
            }
            try {
                return new JsonObject((Map<String, Object>) mapper.convertValue(obj, Map.class));
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to map to json", e);
                return new JsonObject().put(collectionKey, obj);
            }
        }

    }

}
