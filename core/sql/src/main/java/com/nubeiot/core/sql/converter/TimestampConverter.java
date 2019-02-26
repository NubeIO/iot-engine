package com.nubeiot.core.sql.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.jooq.Converter;

public final class TimestampConverter implements Converter<Timestamp, LocalDateTime> {

    @Override
    public LocalDateTime from(Timestamp databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(databaseObject.getTime()), ZoneOffset.UTC);
    }

    @Override
    public Timestamp to(LocalDateTime userObject) {
        return Objects.isNull(userObject) ? null : Timestamp.valueOf(userObject);
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<LocalDateTime> toType() {
        return LocalDateTime.class;
    }

}

