package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;

public final class SequenceOfSerializer extends EncodableSerializer<SequenceOf> {

    SequenceOfSerializer() {
        super(SequenceOf.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(SequenceOf value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartArray();
        value.forEach(o -> write(gen, o));
        gen.writeEndArray();
    }

    private void write(JsonGenerator gen, Object o) {
        try {
            gen.writeObject(o);
        } catch (IOException e) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Something is wrong in json writer of {}", e, SequenceOf.class);
            }
        }
    }

}
