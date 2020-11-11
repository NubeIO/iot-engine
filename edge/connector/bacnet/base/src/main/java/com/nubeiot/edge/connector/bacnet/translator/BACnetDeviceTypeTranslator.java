package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import io.github.zero88.utils.Strings;

import com.nubeiot.edge.connector.bacnet.mixin.serializer.EncodableSerializer;
import com.nubeiot.edge.connector.bacnet.translator.BACnetTranslator.BACnetIoTNotionTranslator;
import com.nubeiot.iotdata.dto.DeviceType;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;

public final class BACnetDeviceTypeTranslator implements BACnetIoTNotionTranslator<DeviceType, Encodable> {

    @Override
    public DeviceType serialize(Encodable object) {
        if (Objects.isNull(object) || object instanceof ErrorClassAndCode) {
            return null;
        }
        return DeviceType.factory(Strings.toString(EncodableSerializer.encode(object)));
    }

    @Override
    public Encodable deserialize(DeviceType concept) {
        return null;
    }

    @Override
    public Class<DeviceType> fromType() {
        return DeviceType.class;
    }

    @Override
    public Class<Encodable> toType() {
        return Encodable.class;
    }

}
