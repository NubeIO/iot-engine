package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

public final class ObjectIdentifierValueSerializer extends EncodableSerializer<ObjectIdentifier>
    implements ObjectIdentifierMixin {

    ObjectIdentifierValueSerializer() {
        super(ObjectIdentifier.class);
    }

    @Override
    public void serialize(ObjectIdentifier value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(ObjectIdentifierMixin.serialize(value));
    }

}
