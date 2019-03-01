package com.nubeiot.core.sql.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimestampConverterTest {

    private TimestampConverter converter;

    @Before
    public void before() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        this.converter = new TimestampConverter();
    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test
    public void test_from_timezone() {
        Timestamp timestamp = Timestamp.valueOf("2018-12-03 05:15:30");
        LocalDateTime localDateTime = this.converter.from(timestamp);

        Instant instant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2018-12-03T05:15:30+00:00"));
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

        Assert.assertEquals(dateTime, localDateTime);
    }

    @Test
    public void test_to_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_timezone() {
        Instant instant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2018-12-03T05:15:30+00:00"));
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        Timestamp localTimestamp = this.converter.to(dateTime);

        Timestamp timestamp = Timestamp.valueOf("2018-12-03 05:15:30");
        Assert.assertEquals(timestamp, localTimestamp);
    }

    @Test
    public void test_from_class() {
        Assert.assertEquals(this.converter.fromType(), Timestamp.class);
    }

    @Test
    public void test_to_class() {
        Assert.assertEquals(this.converter.toType(), LocalDateTime.class);
    }

}
