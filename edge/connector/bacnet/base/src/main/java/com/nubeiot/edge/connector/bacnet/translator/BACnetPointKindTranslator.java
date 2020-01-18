package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.PointKind;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;

public final class BACnetPointKindTranslator implements BACnetIoTNotionTranslator<PointKind, ObjectType> {

    @Override
    public PointKind serialize(ObjectType object) {
        if (Objects.isNull(object)) {
            return PointKind.UNKNOWN;
        }
        if (object.isOneOf(ObjectType.analogInput, ObjectType.binaryInput, ObjectType.multiStateInput)) {
            return PointKind.INPUT;
        }
        if (object.isOneOf(ObjectType.analogOutput, ObjectType.binaryOutput, ObjectType.binaryLightingOutput,
                           ObjectType.lightingOutput, ObjectType.multiStateOutput)) {
            return PointKind.OUTPUT;
        }
        if (object.isOneOf(ObjectType.analogValue, ObjectType.largeAnalogValue)) {
            return PointKind.SET_POINT;
        }
        if (object.isOneOf(ObjectType.command)) {
            return PointKind.COMMAND;
        }
        throw new IllegalArgumentException("Invalid point kind");
    }

    @Override
    public ObjectType deserialize(PointKind concept) {
        return null;
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
