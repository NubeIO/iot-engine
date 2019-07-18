package com.nubeiot.iotdata.converter;

import com.nubeiot.iotdata.dto.PointCategory;

public final class PointCategoryConverter extends AbstractEnumConverter<PointCategory> {

    @Override
    protected PointCategory def() {
        return PointCategory.def();
    }

    @Override
    public Class<PointCategory> toType() { return PointCategory.class; }

}
