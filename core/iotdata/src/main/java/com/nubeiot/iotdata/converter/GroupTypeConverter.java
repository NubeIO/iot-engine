package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.GroupType;

public final class GroupTypeConverter extends AbstractEnumConverter<GroupType> {

    @Override
    protected GroupType def() { return null; }

    @Override
    public Class<GroupType> toType() { return GroupType.class; }

}
