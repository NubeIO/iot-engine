package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.ThingType;
import com.serotonin.bacnet4j.type.Encodable;

public class BACnetTransducerTypeTranslator implements BACnetIoTNotionTranslator<ThingType, Encodable> {

    @Override
    public ThingType serialize(Encodable object) {
        return null;
    }

    @Override
    public Encodable deserialize(ThingType concept) {
        return null;
    }

    @Override
    public Class<ThingType> fromType() {
        return ThingType.class;
    }

    @Override
    public Class<Encodable> toType() {
        return Encodable.class;
    }

}
