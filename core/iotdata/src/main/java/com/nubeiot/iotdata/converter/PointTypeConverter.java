package com.nubeiot.iotdata.converter;

import com.nubeiot.iotdata.dto.PointType;

public final class PointTypeConverter extends AbstractEnumConverter<PointType> {

    @Override
    protected PointType def() { return PointType.def(); }

    @Override
    public Class<PointType> toType() { return PointType.class; }

}
