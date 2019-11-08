package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.serotonin.bacnet4j.type.Encodable;

import lombok.NonNull;

public abstract class EncodableSerializer<T extends Encodable> extends StdSerializer<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EncodableSerializer.class);

    static final EncodableSerializer<Encodable> DEFAULT = new EncodableSerializer<Encodable>(Encodable.class) {};

    EncodableSerializer(@NonNull Class<T> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }

}
