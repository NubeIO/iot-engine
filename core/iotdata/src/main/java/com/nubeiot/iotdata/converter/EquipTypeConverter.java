package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.EquipType;

public final class EquipTypeConverter extends AbstractEnumConverter<EquipType> {

    @Override
    protected EquipType def() { return null; }

    @Override
    public Class<EquipType> toType() { return EquipType.class; }

}
