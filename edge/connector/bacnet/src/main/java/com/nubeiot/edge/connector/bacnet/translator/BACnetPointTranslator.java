package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

//TODO implement it
public class BACnetPointTranslator implements BACnetTranslator<PointComposite, PropertyValuesMixin>, IoTEntityTranslator<PointComposite, PropertyValuesMixin> {

    @Override
    public PointComposite serialize(PropertyValuesMixin object) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException("Invalid object properties. Cannot convert to persistence data");
        }
        return null;
    }

    @Override
    public PropertyValuesMixin deserialize(PointComposite concept) {
        return null;
    }

    @Override
    public Class<PointComposite> fromType() {
        return PointComposite.class;
    }

    @Override
    public Class<PropertyValuesMixin> toType() {
        return PropertyValuesMixin.class;
    }

}
