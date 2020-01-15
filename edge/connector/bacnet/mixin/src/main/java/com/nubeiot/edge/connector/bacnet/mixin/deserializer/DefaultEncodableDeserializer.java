package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Functions;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.util.sero.ByteQueue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class DefaultEncodableDeserializer implements EncodableDeserializer<Encodable, Object> {

    private final Class<? extends Encodable> actualEncodableClass;

    static byte[] serialize(Object obj) throws IOException {
        if (obj instanceof JsonObject) {
            return ((JsonObject) obj).toBuffer().getBytes();
        }
        if (obj instanceof JsonArray) {
            return ((JsonArray) obj).toBuffer().getBytes();
        }
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    @Override
    public @NonNull Class<Encodable> encodableClass() {
        return Encodable.class;
    }

    @Override
    public @NonNull Class<Object> javaClass() {
        return Object.class;
    }

    @Override
    public Encodable parse(@NonNull Object value) {
        final ByteQueue queue = Functions.getOrDefault((ByteQueue) null, () -> new ByteQueue(serialize(value)));
        try {
            return Encodable.read(queue, actualEncodableClass);
        } catch (BACnetException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
