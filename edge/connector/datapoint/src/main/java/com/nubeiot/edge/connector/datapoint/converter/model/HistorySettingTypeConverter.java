package com.nubeiot.edge.connector.datapoint.converter.model;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.edge.connector.datapoint.dto.HistorySettingType;

public final class HistorySettingTypeConverter implements Converter<String, HistorySettingType> {

    @Override
    public HistorySettingType from(String databaseObject) { return HistorySettingType.factory(databaseObject); }

    @Override
    public String to(HistorySettingType userObject) {
        return Objects.isNull(userObject) ? HistorySettingType.def().type() : userObject.type();
    }

    @Override
    public Class<String> fromType() { return String.class; }

    @Override
    public Class<HistorySettingType> toType() { return HistorySettingType.class; }

}
