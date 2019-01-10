package com.nubeiot.core.sql.converter;

import java.time.LocalDate;
import java.util.Objects;

import org.jooq.Converter;

public final class DateConverter implements Converter<java.sql.Date, LocalDate> {

    @Override
    public LocalDate from(java.sql.Date databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        return LocalDate.ofEpochDay(databaseObject.getTime());
    }

    @Override
    public java.sql.Date to(LocalDate userObject) {
        return Objects.isNull(userObject) ? null : new java.sql.Date(userObject.toEpochDay());
    }

    @Override
    public Class<java.sql.Date> fromType() {
        return java.sql.Date.class;
    }

    @Override
    public Class<LocalDate> toType() {
        return LocalDate.class;
    }

}

