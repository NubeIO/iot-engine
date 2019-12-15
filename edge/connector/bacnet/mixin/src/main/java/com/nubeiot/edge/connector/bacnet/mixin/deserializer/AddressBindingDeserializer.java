package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.mixin.AddressMixin;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.type.constructed.AddressBinding;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class AddressBindingDeserializer extends BaseTypeDeserializer<AddressBinding> {

    AddressBindingDeserializer() {
        super(AddressBinding.class);
    }

    @Override
    public AddressBinding parse(@NonNull JsonObject value) {
        ObjectIdentifier deviceObjectIdentifier = ObjectIdentifierMixin.deserialize(
            value.getString("device-object-identifier", ""));
        AddressMixin addressMixin = AddressMixin.create(value.getJsonObject("device-address"));
        return new AddressBinding(deviceObjectIdentifier, addressMixin.unwrap());
    }

}
