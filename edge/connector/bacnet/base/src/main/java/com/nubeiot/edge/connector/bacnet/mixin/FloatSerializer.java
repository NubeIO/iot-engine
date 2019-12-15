package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.primitive.Real;

public final class FloatSerializer extends EncodableSerializer<Real> {

    FloatSerializer() {
        super(Real.class);
    }

    @Override
    public void serialize(Real value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(value.floatValue());
    }

}
