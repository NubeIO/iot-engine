package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.DeviceType;

public final class DeviceTypeConverter extends AbstractEnumConverter<DeviceType> {

    @Override
    protected DeviceType def() { return null; }

    @Override
    public Class<DeviceType> toType() { return DeviceType.class; }

}
