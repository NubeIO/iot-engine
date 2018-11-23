package com.nubeiot.edge.core.model.converter;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import org.jooq.Converter;

public final class DateConverter implements Converter<Timestamp, Date> {

    @Override
    public Date from(Timestamp databaseObject) {
        if (Objects.isNull(databaseObject)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        calendar.setTimeInMillis(databaseObject.getTime());
        return calendar.getTime();
    }

    @Override
    public Timestamp to(Date userObject) {
        return Objects.isNull(userObject) ? null : new Timestamp(userObject.getTime());
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<Date> toType() {
        return Date.class;
    }

}
