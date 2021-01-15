package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTChunkNotionTranslator;
import com.nubeiot.iotdata.dto.PointType;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;

public final class BACnetPointTypeTranslator implements BACnetIoTChunkNotionTranslator<PointType, ObjectType> {

    @Override
    public PointType serialize(ObjectType objectType) {
        Objects.requireNonNull(objectType, "Invalid BACnet object type");
        if (objectType.isOneOf(ObjectType.analogInput, ObjectType.analogOutput, ObjectType.analogValue,
                               ObjectType.largeAnalogValue)) {
            return PointType.ANALOG;
        }
        if (objectType.isOneOf(ObjectType.binaryInput, ObjectType.binaryOutput, ObjectType.binaryValue,
                               ObjectType.binaryLightingOutput)) {
            return PointType.DIGITAL;
        }
        return PointType.UNKNOWN;
    }

    @Override
    public Class<PointType> fromType() {
        return PointType.class;
    }

    @Override
    public Class<ObjectType> toType() {
        return ObjectType.class;
    }

}
