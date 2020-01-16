package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import java.util.function.Function;
import java.util.function.Supplier;

import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Reflections.ReflectionMethod;
import com.nubeiot.core.utils.Strings;
import com.serotonin.bacnet4j.type.primitive.Enumerated;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class EnumeratedDeserializer<T extends Enumerated> implements EncodableDeserializer<T, String> {

    @NonNull
    private final Class<T> enumeratedClass;

    @Override
    public @NonNull Class<T> encodableClass() {
        return enumeratedClass;
    }

    @Override
    public @NonNull Class<String> javaClass() {
        return String.class;
    }

    @Override
    public String cast(@NonNull Object value) {
        return Strings.toString(value);
    }

    @Override
    public T parse(@NonNull String value) {
        final Supplier<T> forName = () -> ReflectionMethod.executeStatic(encodableClass(), "forName", value);
        final Function<Integer, T> forId = id -> ReflectionMethod.executeStatic(encodableClass(), "forId", id);
        return Functions.getIfThrow(() -> Functions.toInt().apply(value))
                        .map(id -> Functions.getOrDefault(() -> forId.apply(id), forName))
                        .orElseGet(forName);
    }

}
