package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.Enumerated;

public final class EnumeratedSerializer extends EncodableSerializer<Enumerated> {

    EnumeratedSerializer() {
        super(Enumerated.class);
    }

    @Override
    public void serialize(Enumerated value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        serializeIfAnyErrorFallback(this::serialize, value, gen);
    }

    private void serialize(Enumerated value, JsonGenerator gen) throws IOException {
        if (value instanceof ObjectType) {
            gen.writeString(value.toString());
            return;
        }
        final Map<String, Object> map = new HashMap<>();
        map.put("value", value.toString());
        map.put("rawValue", value.intValue());
        gen.writeObject(map);
    }

}
