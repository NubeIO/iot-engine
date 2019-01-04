package com.nubeiot.core.sql.converter;

import java.time.LocalTime;
import java.util.Objects;

import org.jooq.Converter;

public final class TimeConverter implements Converter<java.sql.Date, LocalTime> {

    @Override
    public LocalTime from(java.sql.Date databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        return LocalTime.ofSecondOfDay(databaseObject.getTime() / 1000);
    }

    @Override
    public java.sql.Date to(LocalTime userObject) {
        return Objects.isNull(userObject) ? null : new java.sql.Date(userObject.toSecondOfDay() * 1000);
    }

    @Override
    public Class<java.sql.Date> fromType() {
        return java.sql.Date.class;
    }

    @Override
    public Class<LocalTime> toType() {
        return LocalTime.class;
    }

}

