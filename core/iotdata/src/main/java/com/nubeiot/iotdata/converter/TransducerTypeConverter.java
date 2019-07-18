package com.nubeiot.iotdata.converter;

import com.nubeiot.iotdata.dto.TransducerType;

public final class TransducerTypeConverter extends AbstractEnumConverter<TransducerType> {

    @Override
    protected TransducerType def() { return TransducerType.def(); }

    @Override
    public Class<TransducerType> toType() { return TransducerType.class; }

}
