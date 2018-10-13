package com.nubeio.iot.share.utils;

import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimes {

    public static Date now() {
        return Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).getTime();
    }

}
