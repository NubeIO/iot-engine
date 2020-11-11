package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import io.github.zero88.utils.Reflections.ReflectionMethod;

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
    public @NonNull Class<String> fromClass() {
        return String.class;
    }

    @Override
    public T parse(@NonNull String value) {
        return ReflectionMethod.executeStatic(encodableClass(), "forName", value);
    }

}
