package com.nubeiot.iotdata.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.iotdata.dto.PointCategory;

public final class PointCategoryConverter implements Converter<String, PointCategory> {

    @Override
    public PointCategory from(String databaseObject) {
        return PointCategory.factory(databaseObject);
    }

    @Override
    public String to(PointCategory userObject) {
        return Objects.isNull(userObject) ? PointCategory.def().type() : userObject.type();
    }

    @Override
    public Class<String> fromType() { return String.class; }

    @Override
    public Class<PointCategory> toType() { return PointCategory.class; }

}
