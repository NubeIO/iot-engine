package com.nubeiot.edge.connector.bacnet.converter;

import java.util.Arrays;
import java.util.List;

import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

public final class BACnetPointValueConverter /*implements BACnetTranslator<PointValueData, PropertyValuesMixin>,
                                                        IoTEntityConverter<PointValueData, PropertyValuesMixin>*/ {

    static final List<PropertyIdentifier> DATA_PROPS = Arrays.asList(PropertyIdentifier.presentValue,
                                                                     PropertyIdentifier.priorityArray);

    //    @Override
    //    public PointValueData serialize(PropertyValuesMixin mixin) {
    //        final Optional<PriorityArray> arrayValues = mixin.getAndCast(PropertyIdentifier.priorityArray);
    //        final PointPriorityValue value = new BACnetPointPriorityValueTranslator().serialize(arrayValues.orElse
    //        (null));
    //        final PointValue highestValue = value.findHighestValue();
    //        return new PointValueData().setPriority(highestValue.getPriority())
    //                                   .setValue(highestValue.getValue())
    //                                   .setPriorityValues(value);
    //    }
    //
    //    @Override
    //    public PropertyValuesMixin deserialize(PointValueData concept) {
    //        return null;
    //    }
    //
    //    @Override
    //    public @NonNull Class<PointValueData> fromType() {
    //        return PointValueData.class;
    //    }
    //
    //    @Override
    //    public @NonNull Class<PropertyValuesMixin> toType() {
    //        return PropertyValuesMixin.class;
    //    }
}
