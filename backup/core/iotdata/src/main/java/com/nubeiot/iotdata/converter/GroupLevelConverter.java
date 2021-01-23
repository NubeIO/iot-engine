package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.GroupLevel;

public final class GroupLevelConverter extends AbstractEnumConverter<GroupLevel> {

    @Override
    protected GroupLevel def() { return null; }

    @Override
    public Class<GroupLevel> toType() { return GroupLevel.class; }

}
