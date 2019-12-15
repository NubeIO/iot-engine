package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.PointType;
import com.serotonin.bacnet4j.type.Encodable;

public final class BACnetPointTypeTranslator implements BACnetIoTNotionTranslator<PointType, Encodable> {

    @Override
    public PointType serialize(Encodable object) {
        return null;
    }

    @Override
    public Encodable deserialize(PointType concept) {
        return null;
    }

    @Override
    public Class<PointType> fromType() {
        return PointType.class;
    }

    @Override
    public Class<Encodable> toType() {
        return Encodable.class;
    }

}
