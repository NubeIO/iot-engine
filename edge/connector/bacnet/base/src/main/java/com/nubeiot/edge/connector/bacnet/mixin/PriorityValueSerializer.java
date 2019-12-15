package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.constructed.PriorityValue;

public final class PriorityValueSerializer extends EncodableSerializer<PriorityValue> {

    PriorityValueSerializer() {
        super(PriorityValue.class);
    }

    @Override
    public void serialize(PriorityValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(value.getConstructedValue());
    }

}
