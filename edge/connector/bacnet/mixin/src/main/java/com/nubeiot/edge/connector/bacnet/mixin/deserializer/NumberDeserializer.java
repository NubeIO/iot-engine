package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import com.serotonin.bacnet4j.type.Encodable;

import lombok.NonNull;

public interface NumberDeserializer<T extends Encodable, V extends Number> extends EncodableDeserializer<T, V> {

    static Long castToLong(@NonNull Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Invalid number: " + value);
    }

    static Integer castToInt(@NonNull Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Invalid number: " + value);
    }

    static Double castToDouble(@NonNull Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Invalid number: " + value);
    }

    static Float castToFloat(@NonNull Object value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new IllegalArgumentException("Invalid number: " + value);
    }

    static Short castToShort(@NonNull Object value) {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        throw new IllegalArgumentException("Invalid number: " + value);
    }

}
