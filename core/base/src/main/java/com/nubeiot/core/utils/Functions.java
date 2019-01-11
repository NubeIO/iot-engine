package com.nubeiot.core.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.HiddenException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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

}
