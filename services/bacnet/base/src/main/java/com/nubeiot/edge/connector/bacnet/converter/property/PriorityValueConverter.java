package com.nubeiot.edge.connector.bacnet.converter.property;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.github.zero88.qwe.iot.data.converter.IoTPropertyConverter;
import io.github.zero88.qwe.iot.data.property.PointPriorityValue;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.NonNull;

//TODO implement it
public class PriorityValueConverter implements IoTPropertyConverter<PointPriorityValue, PriorityArray>, BACnetProtocol {

    static final List<PropertyIdentifier> DATA_PROPS = Arrays.asList(PropertyIdentifier.presentValue,
                                                                     PropertyIdentifier.priorityArray);

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
