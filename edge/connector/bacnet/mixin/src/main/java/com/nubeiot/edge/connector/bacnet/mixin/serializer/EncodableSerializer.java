package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import java.io.IOException;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Objects;

import io.reactivex.functions.BiConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetMixin;
import com.serotonin.bacnet4j.type.Encodable;

import lombok.NonNull;

public abstract class EncodableSerializer<T extends Encodable> extends StdSerializer<T> {

    public static final EncodableSerializer<Encodable> DEFAULT = new EncodableSerializer<Encodable>(Encodable.class) {};
    static final Logger LOGGER = LoggerFactory.getLogger(EncodableSerializer.class);

    EncodableSerializer(@NonNull Class<T> clazz) {
        super(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T encode(Encodable encodable) {
        if (Objects.isNull(encodable)) {
            return null;
        }
        return (T) BACnetMixin.MAPPER.convertValue(Collections.singletonMap("encode", encodable), JsonObject.class)
                                     .stream()
                                     .map(Entry::getValue)
                                     .findFirst()
                                     .orElse(null);
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        defaultSerialize(value, gen);
    }

    private void defaultSerialize(T value, JsonGenerator gen) throws IOException {
        gen.writeString(value.toString());
    }

    void serializeIfAnyErrorFallback(BiConsumer<T, JsonGenerator> write, T v, JsonGenerator gen) throws IOException {
        try {
            write.accept(v, gen);
        } catch (Exception e) {
            LOGGER.warn(e);
            defaultSerialize(v, gen);
        }
    }

}
