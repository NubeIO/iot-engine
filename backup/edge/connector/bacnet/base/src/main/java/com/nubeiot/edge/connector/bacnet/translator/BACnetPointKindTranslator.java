package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTChunkNotionTranslator;
import com.nubeiot.iotdata.dto.PointKind;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;

public final class BACnetPointKindTranslator implements BACnetIoTChunkNotionTranslator<PointKind, ObjectType> {

    @Override
    public PointKind serialize(ObjectType objectType) {
        Objects.requireNonNull(objectType, "Invalid BACnet object type");
        if (objectType.isOneOf(ObjectType.analogInput, ObjectType.binaryInput)) {
            return PointKind.INPUT;
        }
        if (objectType.isOneOf(ObjectType.multiStateInput)) {
            return PointKind.MULTI_STATE_INPUT;
        }
        if (objectType.isOneOf(ObjectType.analogOutput, ObjectType.binaryOutput, ObjectType.binaryLightingOutput,
                               ObjectType.lightingOutput)) {
            return PointKind.OUTPUT;
        }
        if (objectType.isOneOf(ObjectType.multiStateOutput)) {
            return PointKind.MULTI_STATE_OUTPUT;
        }
        if (objectType.isOneOf(ObjectType.analogValue, ObjectType.largeAnalogValue, ObjectType.binaryValue)) {
            return PointKind.SET_POINT;
        }
        if (objectType.isOneOf(ObjectType.command)) {
            return PointKind.COMMAND;
        }
        return PointKind.factory(objectType.toString());
    }

    @Override
    public Class<PointKind> fromType() {
        return PointKind.class;
    }

    @Override
    public Class<ObjectType> toType() {
        return ObjectType.class;
    }

}
