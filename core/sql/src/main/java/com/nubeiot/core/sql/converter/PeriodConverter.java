package com.nubeiot.core.sql.converter;

import java.time.Period;
import java.time.format.DateTimeParseException;

import org.jooq.Converter;

import com.nubeiot.core.exceptions.DatabaseException;

public final class PeriodConverter implements Converter<String, Period> {

    @Override
    public Period from(String databaseObject) {
        try {
            return Period.parse(databaseObject);
        } catch (DateTimeParseException e) {
            throw new DatabaseException("Wrong Period data format: " + databaseObject, e);
        }
    }

    @Override
    public String to(Period userObject) { return userObject.toString(); }

    @Override
    public Class<String> fromType() { return String.class; }

    @Override
    public Class<Period> toType() { return Period.class; }

}
