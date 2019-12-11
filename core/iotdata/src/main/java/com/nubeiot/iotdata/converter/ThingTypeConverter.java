package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.ThingType;

public final class ThingTypeConverter extends AbstractEnumConverter<ThingType> {

    @Override
    protected ThingType def() { return ThingType.def(); }

    @Override
    public Class<ThingType> toType() { return ThingType.class; }

}
