package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nubeiot.edge.connector.bacnet.mixin.AddressMixin;
import com.serotonin.bacnet4j.type.constructed.Address;

public final class AddressSerializer extends EncodableSerializer<Address> {

    AddressSerializer() {
        super(Address.class);
    }

    @Override
    public void serialize(Address value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(AddressMixin.create(value));
    }

}
