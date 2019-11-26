package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.PointKind;
import com.serotonin.bacnet4j.type.Encodable;

public final class BACnetPointKindTranslator implements BACnetIoTNotionTranslator<PointKind, Encodable> {

    @Override
    public PointKind serialize(Encodable object) {
        return null;
    }

    @Override
    public Encodable deserialize(PointKind concept) {
        return null;
    }

    @Override
    public Class<PointKind> fromType() {
        return PointKind.class;
    }

    @Override
    public Class<Encodable> toType() {
        return Encodable.class;
    }

}
