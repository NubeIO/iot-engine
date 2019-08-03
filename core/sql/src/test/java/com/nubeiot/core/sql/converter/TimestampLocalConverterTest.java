package com.nubeiot.core.sql.converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimestampLocalConverterTest {

    private TimestampLocalConverter converter;

    @Before
    public void before() {
        this.converter = new TimestampLocalConverter();
    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test
    public void test_from_timezone_utc() {
        Timestamp timestamp = Timestamp.valueOf("2018-12-03 05:15:30");
        LocalDateTime fromValue = this.converter.from(timestamp);
        LocalDateTime expected = OffsetDateTime.parse("2018-12-03T05:15:30+00:00",
                                                      DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
        Assert.assertEquals(expected, fromValue);
    }

    @Test
    public void test_to_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_timezone() {
        LocalDateTime dateTime = OffsetDateTime.parse("2018-12-03T05:15:30+00:00",
                                                      DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
        Timestamp toValue = this.converter.to(dateTime);
        Timestamp expected = Timestamp.valueOf("2018-12-03 05:15:30");
        Assert.assertEquals(expected, toValue);
    }

    @Test
    public void test_to_timezone_from_system_zone() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
            LocalDateTime.parse("2018-12-03T05:15:30", DateTimeFormatter.ISO_LOCAL_DATE_TIME), ZoneId.systemDefault());
        Assert.assertEquals(ZoneId.systemDefault(), zonedDateTime.getZone());
        LocalDateTime dateTime = zonedDateTime.toLocalDateTime();
        Timestamp toValue = this.converter.to(dateTime);
        Timestamp expected = Timestamp.valueOf("2018-12-03 05:15:30");
        Assert.assertEquals(expected, toValue);
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
