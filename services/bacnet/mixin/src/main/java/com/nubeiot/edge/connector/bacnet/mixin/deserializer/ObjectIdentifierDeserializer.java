package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class ObjectIdentifierDeserializer implements EncodableDeserializer<ObjectIdentifier, String> {

    @Override
    public @NonNull Class<ObjectIdentifier> encodableClass() {
        return ObjectIdentifier.class;
    }

    @Override
    public @NonNull Class<String> javaClass() {
        return String.class;
    }

    @Override
    public ObjectIdentifier parse(@NonNull String value) {
        return ObjectIdentifierMixin.deserialize(value);
    }

}
