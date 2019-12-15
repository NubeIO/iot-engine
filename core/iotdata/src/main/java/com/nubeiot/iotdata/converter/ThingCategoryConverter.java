package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.ThingCategory;

public final class ThingCategoryConverter extends AbstractEnumConverter<ThingCategory> {

    @Override
    protected ThingCategory def() { return null; }

    @Override
    public Class<ThingCategory> toType() { return ThingCategory.class; }

}
