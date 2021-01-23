package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.TransducerCategory;

public final class TransducerCategoryConverter extends AbstractEnumConverter<TransducerCategory> {

    @Override
    protected TransducerCategory def() { return null; }

    @Override
    public Class<TransducerCategory> toType() { return TransducerCategory.class; }

}
