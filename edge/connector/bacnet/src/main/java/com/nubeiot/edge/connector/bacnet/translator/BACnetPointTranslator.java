package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.edge.connector.bacnet.dto.PropertyValuesMixin;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

public class BACnetPointTranslator implements BACnetTranslator<PointComposite, PropertyValuesMixin>,
                                              IoTEntityTranslator<PointComposite, PropertyValuesMixin> {

    @Override
    public PropertyValuesMixin from(PointComposite concept) {
        return null;
    }

    @Override
    public PointComposite to(PropertyValuesMixin object) {
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
