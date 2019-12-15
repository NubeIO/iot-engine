package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.constructed.Recipient;

public final class RecipientSerializer extends EncodableSerializer<Recipient> {

    RecipientSerializer() {
        super(Recipient.class);
    }

    @Override

    public void serialize(Recipient value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(value.getValue());
    }

}
