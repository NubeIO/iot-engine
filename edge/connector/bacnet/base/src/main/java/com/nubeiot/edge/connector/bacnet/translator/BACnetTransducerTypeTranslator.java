package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTChunkNotionTranslator;
import com.nubeiot.iotdata.dto.TransducerType;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;

public class BACnetTransducerTypeTranslator implements BACnetIoTChunkNotionTranslator<TransducerType, ObjectType> {

    @Override
    public TransducerType serialize(ObjectType objectType) {
        Objects.requireNonNull(objectType, "Invalid BACnet object type");
        if (objectType.isOneOf(ObjectType.analogInput, ObjectType.analogOutput, ObjectType.analogValue,
                               ObjectType.largeAnalogValue, ObjectType.lightingOutput)) {
            return TransducerType.SENSOR;
        }
        if (objectType.isOneOf(ObjectType.binaryInput, ObjectType.binaryOutput, ObjectType.binaryValue,
                               ObjectType.binaryLightingOutput)) {
            return TransducerType.ACTUATOR;
        }
        return null;
    }

    @Override
    public Class<TransducerType> fromType() {
        return TransducerType.class;
    }

    @Override
    public Class<ObjectType> toType() {
        return ObjectType.class;
    }

}
