package com.nubeiot.core.sql.converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimestampConverterTest {

    private TimestampConverter converter;
    //    private TimeZone zoneDef;

    @Before
    public void before() {
        //        zoneDef = TimeZone.getDefault();
        //        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        this.converter = new TimestampConverter();
    }
    //
    //    @After
    //    public void after() {
    //        TimeZone.setDefault(zoneDef);
    //    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test
    public void test_from_timezone() {
        Timestamp timestamp = Timestamp.valueOf("2018-12-03 05:15:30");
        OffsetDateTime localDateTime = this.converter.from(timestamp);

        Instant instant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2018-12-03T05:15:30+00:00"));
        OffsetDateTime expected = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);

        Assert.assertEquals(expected, localDateTime);
    }

    @Test
    public void test_to_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_timezone() {
        Instant instant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2018-12-03T05:15:30+00:00"));
        OffsetDateTime dateTime = OffsetDateTime.ofInstant(instant, TimeZone.getTimeZone(ZoneOffset.UTC).toZoneId());
        Timestamp localTimestamp = this.converter.to(dateTime);

        Timestamp expected = Timestamp.valueOf("2018-12-03 05:15:30");
        Assert.assertEquals(expected, localTimestamp);
    }

    @Test
    public void test_from_class() {
        Assert.assertEquals(this.converter.fromType(), Timestamp.class);
    }

    @Test
    public void test_to_class() {
        Assert.assertEquals(this.converter.toType(), OffsetDateTime.class);
    }

}
