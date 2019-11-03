package com.nubeiot.edge.connector.bacnet.converter;

import com.nubeiot.core.utils.Functions;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectIdentifierConverter {

    private static final String SEPARATOR = ":";

    public static String toRequestId(@NonNull ObjectIdentifier objId) {
        return objId.getObjectType().toString() + SEPARATOR + objId.getInstanceNumber();
    }

    public static ObjectIdentifier toBACnetId(@NonNull String id) {
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

}
