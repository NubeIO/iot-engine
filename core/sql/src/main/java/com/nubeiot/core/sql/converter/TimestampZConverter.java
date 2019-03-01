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
        if (Objects.isNull(userObject)) {
            return null;
        }
        return Timestamp.from(userObject);
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

