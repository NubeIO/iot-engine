package com.nubeiot.iotdata.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.iotdata.unit.UnitLabel;

public class UnitLabelConverter implements Converter<String, UnitLabel> {

    @Override
    public UnitLabel from(String databaseObject) {
        return Strings.isBlank(databaseObject) ? null : JsonData.from(databaseObject, UnitLabel.class);
    }

    @Override
    public String to(UnitLabel userObject) {
        return Objects.isNull(userObject) ? null : userObject.toJson().encode();
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<UnitLabel> toType() {
        return UnitLabel.class;
    }

}
