package com.nubeiot.core.sql.converter;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.jooq.Converter;

public final class DateConverter implements Converter<java.sql.Date, LocalDate> {

    @Override
    public LocalDate from(java.sql.Date databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        return LocalDate.ofEpochDay(TimeUnit.MILLISECONDS.toDays(databaseObject.getTime()));
    }

    @Override
    public java.sql.Date to(LocalDate userObject) {
        return Objects.isNull(userObject) ? null : Date.valueOf(userObject);
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

