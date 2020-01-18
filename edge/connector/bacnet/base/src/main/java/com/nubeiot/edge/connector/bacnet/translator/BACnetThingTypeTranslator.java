package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.ThingType;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;

public class BACnetThingTypeTranslator implements BACnetIoTNotionTranslator<ThingType, ObjectType> {

    @Override
    public ThingType serialize(ObjectType object) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException("Unknown BACnet object type");
        }
        return null;
    }

    @Override
    public ObjectType deserialize(ThingType concept) {
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
