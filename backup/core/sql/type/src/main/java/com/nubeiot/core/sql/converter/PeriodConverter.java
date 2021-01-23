package com.nubeiot.core.sql.converter;

import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.core.exceptions.DatabaseException;

public final class PeriodConverter implements Converter<String, Period> {

    @Override
    public Period from(String databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        try {
            return Period.parse(databaseObject);
        } catch (DateTimeParseException e) {
            throw new DatabaseException("Wrong Period data format: " + databaseObject, e);
        }
    }

    @Override
    public String to(Period userObject) {
        if (Objects.isNull(userObject)) {
            return null;
        }
        return userObject.toString();
    }

    @Override
    public Class<String> fromType() { return String.class; }

    @Override
    public Class<Period> toType() { return Period.class; }

}
