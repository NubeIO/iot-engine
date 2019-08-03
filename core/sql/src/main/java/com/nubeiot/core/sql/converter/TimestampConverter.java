package com.nubeiot.core.sql.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.jooq.Converter;

public final class TimestampConverter implements Converter<Timestamp, OffsetDateTime> {

    @Override
    public OffsetDateTime from(Timestamp databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(databaseObject.getTime()), ZoneOffset.UTC);
    }

    @Override
    public Timestamp to(OffsetDateTime userObject) {
        return Objects.isNull(userObject)
               ? null
               : Timestamp.valueOf(userObject.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<OffsetDateTime> toType() {
        return OffsetDateTime.class;
    }

}

