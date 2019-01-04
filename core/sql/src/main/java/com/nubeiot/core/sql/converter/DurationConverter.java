package com.nubeiot.core.sql.converter;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import org.jooq.Converter;

import com.nubeiot.core.exceptions.DatabaseException;

public final class DurationConverter implements Converter<String, Duration> {

    @Override
    public Duration from(String databaseObject) {
        try {
            return Duration.parse(databaseObject);
        } catch (DateTimeParseException e) {
            throw new DatabaseException("Wrong Duration data format: " + databaseObject, e);
        }
    }

    @Override
    public String to(Duration userObject) { return userObject.toString(); }

    @Override
    public Class<String> fromType() { return String.class; }

    @Override
    public Class<Duration> toType() { return Duration.class; }

}
