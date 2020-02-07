package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTChunkNotionTranslator;
import com.nubeiot.iotdata.dto.ThingType;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;

public class BACnetThingTypeTranslator implements BACnetIoTChunkNotionTranslator<ThingType, ObjectType> {

    @Override
    public ThingType serialize(ObjectType objectType) {
        Objects.requireNonNull(objectType, "Invalid BACnet object type");
        if (objectType.isOneOf(ObjectType.analogInput, ObjectType.analogOutput, ObjectType.analogValue,
                               ObjectType.largeAnalogValue, ObjectType.lightingOutput)) {
            return ThingType.SENSOR;
        }
        if (objectType.isOneOf(ObjectType.binaryInput, ObjectType.binaryOutput, ObjectType.binaryValue,
                               ObjectType.binaryLightingOutput)) {
            return ThingType.ACTUATOR;
        }
        return null;
    }

    @Override
    public Class<ThingType> fromType() {
        return ThingType.class;
    }

    @Override
    public Class<ObjectType> toType() {
        return ObjectType.class;
    }

}
