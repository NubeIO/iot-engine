package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.dto.PointPriorityValue.PointValue;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.NonNull;

public final class BACnetPointValueTranslator implements BACnetTranslator<PointValueData, PropertyValuesMixin>,
                                                         IoTEntityTranslator<PointValueData, PropertyValuesMixin> {

    static final List<PropertyIdentifier> DATA_PROPS = Arrays.asList(PropertyIdentifier.presentValue,
                                                                     PropertyIdentifier.priorityArray);

    @Override
    public PointValueData serialize(PropertyValuesMixin mixin) {
        final Optional<PriorityArray> arrayValues = mixin.getAndCast(PropertyIdentifier.priorityArray);
        final PointPriorityValue value = new BACnetPointPriorityValueTranslator().serialize(arrayValues.orElse(null));
        final PointValue highestValue = value.findHighestValue();
        return new PointValueData().setPriority(highestValue.getPriority())
                                   .setValue(highestValue.getValue())
                                   .setPriorityValues(value);
    }

    @Override
    public PropertyValuesMixin deserialize(PointValueData concept) {
        return null;
    }

    @Override
    public @NonNull Class<PointValueData> fromType() {
        return PointValueData.class;
    }

    @Override
    public @NonNull Class<PropertyValuesMixin> toType() {
        return PropertyValuesMixin.class;
    }

}
