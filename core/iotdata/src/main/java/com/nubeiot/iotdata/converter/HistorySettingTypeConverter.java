package com.nubeiot.iotdata.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.iotdata.dto.HistorySettingType;

public final class HistorySettingTypeConverter extends AbstractEnumConverter<HistorySettingType> {

    @Override
    protected HistorySettingType def() { return null; }

    @Override
    public Class<HistorySettingType> toType() { return HistorySettingType.class; }

}
