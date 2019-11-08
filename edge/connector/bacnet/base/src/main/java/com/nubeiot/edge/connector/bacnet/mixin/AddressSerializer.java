package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.constructed.Address;

public final class AddressSerializer extends EncodableSerializer<Address> {

    AddressSerializer() {
        super(Address.class);
    }

    public static String serialize(Address value) {
        return value.getDescription();
    }

    @Override
    public void serialize(Address value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(serialize(value));
    }

}
