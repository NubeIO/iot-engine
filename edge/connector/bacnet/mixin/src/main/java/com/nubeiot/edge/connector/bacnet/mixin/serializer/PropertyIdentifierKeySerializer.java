package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

public final class PropertyIdentifierKeySerializer extends JsonSerializer<PropertyIdentifier> {

    @Override
    public void serialize(PropertyIdentifier value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        gen.writeFieldName(value.toString());
    }

}
