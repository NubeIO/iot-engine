package com.nubeiot.core.sql.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import org.jooq.Converter;

public final class TimestampZConverter implements Converter<Timestamp, Instant> {

    @Override
    public Instant from(Timestamp databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        return Instant.ofEpochMilli(databaseObject.getTime());
    }

    @Override
    public Timestamp to(Instant userObject) {
        return Objects.isNull(userObject) ? null : new Timestamp(userObject.getEpochSecond());
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }

}

