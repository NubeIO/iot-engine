package com.nubeiot.core.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
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

    public static <T> T getOrThrow(@NonNull Supplier<T> provider,
                                   @NonNull Supplier<? extends RuntimeException> override) {
        try {
            return provider.get();
        } catch (Throwable t) {
            throw (RuntimeException) override.get().initCause(t);
        }
    }

    public static <T> T getOrDefault(@NonNull Supplier<T> provider, @NonNull Supplier<T> def) {
        try {
            return provider.get();
        } catch (Throwable t) {
            if (logger.isTraceEnabled()) {
                logger.trace("Fallback default", t);
            }
            return def.get();
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

    public static <T> Function<T, Boolean> to(Predicate<T> predicate) {
        return predicate::test;
    }

    public static Function<String, Integer> toInt() {
        return Integer::valueOf;
    }

    public static Function<String, Long> toLong() {
        return Long::parseLong;
    }

    public static Function<String, UUID> toUUID() {
        return UUID::fromString;
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
