package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.TransducerType;
import com.serotonin.bacnet4j.type.Encodable;

public class BACnetTransducerTypeTranslator implements BACnetIoTNotionTranslator<TransducerType, Encodable> {

    @Override
    public Encodable from(TransducerType concept) {
        return null;
    }

    @Override
    public TransducerType to(Encodable object) {
        return null;
    }

    @Override
    public Class<TransducerType> fromType() {
        return TransducerType.class;
    }

    @Override
    public Class<Encodable> toType() {
        return Encodable.class;
    }

}
