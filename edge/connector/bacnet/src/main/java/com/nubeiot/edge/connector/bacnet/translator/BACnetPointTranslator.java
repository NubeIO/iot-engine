package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.module.datapoint.model.pojos.PointThingComposite;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

//TODO implement it
public final class BACnetPointTranslator implements BACnetTranslator<PointThingComposite, PropertyValuesMixin>,
                                                    IoTEntityTranslator<PointThingComposite, PropertyValuesMixin> {

    @Override
    public PointThingComposite serialize(PropertyValuesMixin object) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException("Invalid object properties. Cannot convert to persistence data");
        }
        return null;
    }

    @Override
    public PropertyValuesMixin deserialize(PointThingComposite concept) {
        return null;
    }

    @Override
    public Class<PointThingComposite> fromType() {
        return PointThingComposite.class;
    }

    @Override
    public Class<PropertyValuesMixin> toType() {
        return PropertyValuesMixin.class;
    }

}
