package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;

import lombok.NonNull;

public final class BACnetPointPriorityValueTranslator
    implements BACnetIoTNotionTranslator<PointPriorityValue, PriorityArray> {

    @Override
    public PointPriorityValue serialize(PriorityArray object) {
        if (Objects.isNull(object)) {
            return new PointPriorityValue();
        }
        return new PointPriorityValue();
    }

    @Override
    public PriorityArray deserialize(PointPriorityValue concept) {
        if (Objects.isNull(concept)) {
            return new PriorityArray();
        }
        return null;
    }

    @Override
    public @NonNull Class<PointPriorityValue> fromType() {
        return PointPriorityValue.class;
    }

    @Override
    public @NonNull Class<PriorityArray> toType() {
        return PriorityArray.class;
    }

}
