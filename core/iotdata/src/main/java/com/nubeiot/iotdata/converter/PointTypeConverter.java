package com.nubeiot.iotdata.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.iotdata.dto.PointType;

public final class PointTypeConverter implements Converter<String, PointType> {

    @Override
    public PointType from(String databaseObject) { return PointType.factory(databaseObject); }

    @Override
    public String to(PointType userObject) {
        return Objects.isNull(userObject) ? PointType.def().type() : userObject.type();
    }

    @Override
    public Class<String> fromType() { return String.class; }

    @Override
    public Class<PointType> toType() { return PointType.class; }

}
