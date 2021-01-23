package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.Protocol;

public final class ProtocolConverter extends AbstractEnumConverter<Protocol> {

    @Override
    protected Protocol def() {
        return Protocol.def();
    }

    @Override
    public Class<Protocol> toType() { return Protocol.class; }

}
