package com.nubeiot.edge.connector.bacnet.converter.property;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.iotdata.converter.IoTPropertyConverter;
import com.nubeiot.iotdata.property.PointValue;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.NonNull;

public final class PointValueConverter
    implements IoTPropertyConverter<PointValue, PropertyValuesMixin>, BACnetProtocol {

    static final List<PropertyIdentifier> DATA_PROPS = Arrays.asList(PropertyIdentifier.presentValue,
                                                                     PropertyIdentifier.priority,
                                                                     PropertyIdentifier.priorityArray);

    @Override
    public PointValue serialize(PropertyValuesMixin mixin) {
        final Integer priority = mixin.encode(PropertyIdentifier.priority);
        final Optional<Encodable> ov = mixin.getAndCast(PropertyIdentifier.presentValue);
        return PointValue.builder()
                         .priority(priority)
                         .value(ov.map(Encodable::toString).orElse(null))
                         .rawValue(ov.map(this::getDoubleValue).orElse(null))
                         .build();
    }

    @Override
    public PropertyValuesMixin deserialize(PointValue concept) {
        return null;
    }

    @Override
    public @NonNull Class<PointValue> fromType() {
        return PointValue.class;
    }

    @Override
    public @NonNull Class<PropertyValuesMixin> toType() {
        return PropertyValuesMixin.class;
    }

    //TODO
    Double getDoubleValue(@NonNull Encodable encodable) {
        return (double) 0;
    }

}
