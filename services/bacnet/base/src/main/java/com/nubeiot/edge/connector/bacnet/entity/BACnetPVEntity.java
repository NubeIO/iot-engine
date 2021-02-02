package com.nubeiot.edge.connector.bacnet.entity;

import io.github.zero88.qwe.iot.data.entity.AbstractPointData;
import io.github.zero88.qwe.iot.data.property.PointPriorityValue;
import io.github.zero88.qwe.iot.data.property.PointValue;

import com.nubeiot.edge.connector.bacnet.converter.property.PointValueConverter;
import com.nubeiot.edge.connector.bacnet.converter.property.PriorityValueConverter;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@SuperBuilder
@Accessors(fluent = true)
public class BACnetPVEntity extends AbstractPointData<ObjectIdentifier> implements BACnetEntity<ObjectIdentifier> {

    public static BACnetPVEntity from(@NonNull BACnetPointEntity point) {
        final PriorityArray pa = point.mixin().<PriorityArray>getAndCast(PropertyIdentifier.priorityArray).orElse(null);
        final PointPriorityValue ppv = new PriorityValueConverter().serialize(pa);
        final PointValue pv = new PointValueConverter().serialize(point.mixin());
        final PointValue highestValue = ppv.findHighestValue();
        final PointValue finalPV = PointValue.builder()
                                             .priority(highestValue.getPriority())
                                             .value(pv.getValue())
                                             .rawValue(pv.getRawValue())
                                             .build();
        return BACnetPVEntity.builder()
                             .key(point.key())
                             .pointId(ObjectIdentifierMixin.serialize(point.key()))
                             .presentValue(finalPV)
                             .priorityValue(ppv)
                             .build();
    }

}
