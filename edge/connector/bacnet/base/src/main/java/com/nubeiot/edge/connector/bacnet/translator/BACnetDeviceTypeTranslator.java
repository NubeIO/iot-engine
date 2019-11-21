package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.mixin.EncodableSerializer;
import com.nubeiot.iotdata.dto.DeviceType;
import com.nubeiot.iotdata.translator.IoTNotionTranslator;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;

public final class BACnetDeviceTypeTranslator
    implements BACnetTranslator<DeviceType, Encodable>, IoTNotionTranslator<DeviceType, Encodable> {

    @Override
    public Encodable from(DeviceType concept) {
        return null;
    }

    @Override
    public DeviceType to(Encodable object) {
        if (Objects.isNull(object) || object instanceof ErrorClassAndCode) {
            return null;
        }
        return DeviceType.factory(Strings.toString(EncodableSerializer.encode(object)));
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
