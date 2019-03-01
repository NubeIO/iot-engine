package com.nubeiot.core.sql.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimestampZConverterTest {

    private TimestampZConverter converter;

    @Before
    public void before() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        this.converter = new TimestampZConverter();
    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test
    public void test_from_timezone() {
        //Timestamp always has no timezone
        Timestamp timestamp = Timestamp.valueOf("2018-12-03 05:15:30");
        Instant convertedInstant = this.converter.from(timestamp);

        Instant instant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2018-12-03T05:15:30+00:00"));

        Assert.assertEquals(instant, convertedInstant);
    }

    @Test
    public void test_to_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_timezone() {
        Instant instant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2018-12-03T05:15:30+00:00"));
        Timestamp localTimestamp = this.converter.to(instant);
        Timestamp timestamp = Timestamp.valueOf("2018-12-03 05:15:30");
        Assert.assertEquals(localTimestamp, timestamp);
    }

    @Test
    public void test_from_class() {
        Assert.assertEquals(this.converter.fromType(), Timestamp.class);
    }

    @Test
    public void test_to_class() {
        Assert.assertEquals(this.converter.toType(), Instant.class);
    }

}
