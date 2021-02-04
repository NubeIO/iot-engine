package com.nubeiot.edge.connector.bacnet.converter.property;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.github.zero88.qwe.iot.data.converter.IoTPropertyConverter;
import io.github.zero88.qwe.iot.data.property.PointPresentValue;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.NonNull;

//TODO implement it
public final class PointValueConverter
    implements IoTPropertyConverter<PointPresentValue, PropertyValuesMixin>, BACnetProtocol {

    static final List<PropertyIdentifier> DATA_PROPS = Arrays.asList(PropertyIdentifier.presentValue,
                                                                     PropertyIdentifier.priorityArray);

    @Override
    public PointPresentValue serialize(PropertyValuesMixin mixin) {
        final Optional<Encodable> ov = mixin.getAndCast(PropertyIdentifier.presentValue);
        return PointPresentValue.builder()
                                .value(ov.map(Encodable::toString).orElse(null))
                                .rawValue(ov.map(this::getDoubleValue).orElse(null))
                                .build();
    }

    @Override
    public PropertyValuesMixin deserialize(PointPresentValue concept) {
        return null;
    }

    @Override
    public @NonNull Class<PointPresentValue> fromType() {
        return PointPresentValue.class;
    }

    @Override
    public @NonNull Class<PropertyValuesMixin> toType() {
        return PropertyValuesMixin.class;
    }

    //TODO implement it
    Double getDoubleValue(@NonNull Encodable encodable) {
        return (double) 0;
    }

}
