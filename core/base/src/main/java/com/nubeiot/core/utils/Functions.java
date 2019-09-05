package com.nubeiot.core.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.HiddenException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Functions {

    private static final Logger logger = LoggerFactory.getLogger(Functions.class);

    public static <T> Optional<T> getIfThrow(@NonNull Supplier<T> provider) {
        return getIfThrow(provider, logger::trace);
    }

    public static <T> Optional<T> getIfThrow(@NonNull Supplier<T> provider, Consumer<Throwable> consumer) {
        try {
            return Optional.ofNullable(provider.get());
        } catch (Throwable t) {
            consumer.accept(t);
            return Optional.empty();
        }
    }

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
