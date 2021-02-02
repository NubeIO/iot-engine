package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;

public class PropertyValueSerializer extends EncodableSerializer<PropertyValue> {

    PropertyValueSerializer() {
        super(PropertyValue.class);
    }

    @Override
    public void serialize(PropertyValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        serializeIfAnyErrorFallback(this::serialize, value, gen);
    }

    private void serialize(PropertyValue value, JsonGenerator gen) throws IOException {
        //TODO implement it
    }

}
