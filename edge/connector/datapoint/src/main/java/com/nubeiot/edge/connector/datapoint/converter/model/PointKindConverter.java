package com.nubeiot.edge.connector.datapoint.converter.model;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.edge.connector.datapoint.dto.PointKind;

public final class PointKindConverter implements Converter<String, PointKind> {

    @Override
    public PointKind from(String databaseObject) { return PointKind.factory(databaseObject); }

    @Override
    public String to(PointKind userObject) {
        return Objects.isNull(userObject) ? PointKind.def().type() : userObject.type();
    }

    @Override
    public Class<String> fromType() { return String.class; }

    @Override
    public Class<PointKind> toType() { return PointKind.class; }

}
