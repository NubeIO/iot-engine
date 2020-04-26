package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.module.datapoint.model.pojos.PointTransducerComposite;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

//TODO implement it
public final class BACnetPointTranslator implements BACnetTranslator<PointTransducerComposite, PropertyValuesMixin>,
                                                    IoTEntityTranslator<PointTransducerComposite, PropertyValuesMixin> {

    @Override
    public PointTransducerComposite serialize(PropertyValuesMixin object) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException("Invalid object properties. Cannot convert to persistence data");
        }
        return null;
    }

    @Override
    public PropertyValuesMixin deserialize(PointTransducerComposite concept) {
        return null;
    }

    @Override
    public Class<PointTransducerComposite> fromType() {
        return PointTransducerComposite.class;
    }

    @Override
    public Class<PropertyValuesMixin> toType() {
        return PropertyValuesMixin.class;
    }

}
