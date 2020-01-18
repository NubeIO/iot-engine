package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.PointType;
import com.serotonin.bacnet4j.type.primitive.CharacterString;

public final class BACnetPointTypeTranslator implements BACnetIoTNotionTranslator<PointType, CharacterString> {

    @Override
    public PointType serialize(CharacterString object) {
        if (Objects.isNull(object)) {
            return PointType.UNKNOWN;
        }
        return PointType.factory(object.getValue());
    }

    @Override
    public CharacterString deserialize(PointType concept) {
        return null;
    }

    @Override
    public Class<PointType> fromType() {
        return PointType.class;
    }

    @Override
    public Class<CharacterString> toType() {
        return CharacterString.class;
    }

}
