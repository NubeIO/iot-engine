package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.module.datapoint.model.pojos.PointTransducerComposite;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

//TODO implement it
/**
 * Represents for Ba cnet point translator.
 *
 * @see <a href="http://www.bacnet.org/Bibliography/ES-7-96/ES-7-96.htm">BACnet-Objects, Properties and Services</a>
 *     <a href="https://www.controlscourse.com/bacnet-objects/">Bacnet objects</a>
 * @since 1.0.0
 */
public final class BACnetPointTranslator implements BACnetTranslator<PointTransducerComposite, PropertyValuesMixin>,
                                                    IoTEntityTranslator<PointTransducerComposite, PropertyValuesMixin> {

    List<PropertyIdentifier> thingProperties = Arrays.asList(PropertyIdentifier.deviceType);

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
