package com.nubeiot.iotdata.converter;

import java.util.Objects;

import org.jooq.Converter;

import io.github.zero.utils.Strings;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.iotdata.dto.PointPriorityValue;

public final class PointPriorityValueConverter implements Converter<String, PointPriorityValue> {

    @Override
    public PointPriorityValue from(String databaseObject) {
        return Strings.isBlank(databaseObject) ? null : JsonData.from(databaseObject, PointPriorityValue.class);
    }

    @Override
    public String to(PointPriorityValue userObject) {
        return Objects.isNull(userObject) ? null : userObject.toJson().encode();
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<PointPriorityValue> toType() {
        return PointPriorityValue.class;
    }

}
