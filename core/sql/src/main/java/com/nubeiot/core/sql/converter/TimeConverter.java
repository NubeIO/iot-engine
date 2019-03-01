package com.nubeiot.core.sql.converter;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Objects;

import org.jooq.Converter;

public final class TimeConverter implements Converter<java.sql.Time, LocalTime> {

    @Override
    public LocalTime from(java.sql.Time databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        return databaseObject.toLocalTime();
    }

    @Override
    public java.sql.Time to(LocalTime userObject) {
        return Objects.isNull(userObject) ? null : Time.valueOf(userObject);
    }

    @Override
    public Class<java.sql.Time> fromType() {
        return java.sql.Time.class;
    }

    @Override
    public Class<LocalTime> toType() {
        return LocalTime.class;
    }

}

