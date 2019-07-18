package com.nubeiot.iotdata.converter;

import com.nubeiot.iotdata.dto.HistorySettingType;

public final class HistorySettingTypeConverter extends AbstractEnumConverter<HistorySettingType> {

    @Override
    protected HistorySettingType def() {
        return HistorySettingType.def();
    }

    @Override
    public Class<HistorySettingType> toType() { return HistorySettingType.class; }

}
