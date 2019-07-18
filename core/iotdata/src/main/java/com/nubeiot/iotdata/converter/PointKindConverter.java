package com.nubeiot.iotdata.converter;

import com.nubeiot.iotdata.dto.PointKind;

public final class PointKindConverter extends AbstractEnumConverter<PointKind> {

    @Override
    protected PointKind def() { return PointKind.def(); }

    @Override
    public Class<PointKind> toType() { return PointKind.class; }

}
