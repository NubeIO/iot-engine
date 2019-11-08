package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nubeiot.core.utils.Functions;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class ObjectIdentifierSerializer extends EncodableSerializer<ObjectIdentifier> {

    private static final String SEPARATOR = ":";

    ObjectIdentifierSerializer() {
        super(ObjectIdentifier.class);
    }

    public static String serialize(@NonNull ObjectIdentifier objId) {
        return objId.getObjectType().toString() + SEPARATOR + objId.getInstanceNumber();
    }

    public static ObjectIdentifier deserialize(@NonNull String id) {
        String[] splitter = id.split(SEPARATOR, 2);
        try {
            final ObjectType objectType = ObjectType.forName(splitter[0]);
            return new ObjectIdentifier(objectType, Functions.getOrThrow(() -> Functions.toInt().apply(splitter[1]),
                                                                         () -> new IllegalArgumentException(
                                                                             "Invalid object identifier format")));
        } catch (BACnetRuntimeException e) {
            throw new IllegalArgumentException("Invalid object identifier format");
        }
    }

    @Override
    public void serialize(ObjectIdentifier value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(serialize(value));
    }

}
