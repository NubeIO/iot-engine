package com.nubeiot.core.sql.converter;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimestampOffsetConverterTest {

    private TimestampOffsetConverter converter;

    @Before
    public void before() {
        this.converter = new TimestampOffsetConverter();
    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test
    public void test_from_timezone_utc() {
        Timestamp timestamp = Timestamp.valueOf("2018-12-03 05:15:30");
        OffsetDateTime fromValue = this.converter.from(timestamp);
        Assert.assertEquals(ZoneOffset.UTC, fromValue.getOffset());
        OffsetDateTime expected = OffsetDateTime.parse("2018-12-03T05:15:30+00:00",
                                                       DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Assert.assertEquals(expected, fromValue);
    }

    @Test
    public void test_to_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_timezone() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2018-12-03T05:15:30+00:00",
                                                       DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Timestamp toValue = this.converter.to(dateTime);
        Timestamp expected = Timestamp.valueOf("2018-12-03 05:15:30");
        Assert.assertEquals(expected, toValue);
    }

    @Test
    public void test_to_timezone_not_utc() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2018-12-03T05:15:30+07:00",
                                                       DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Timestamp toValue = this.converter.to(dateTime);
        Timestamp expected = Timestamp.valueOf(dateTime.withOffsetSameInstant(ZoneOffset.UTC)
                                                       .atZoneSimilarLocal(ZoneId.systemDefault())
                                                       .toLocalDateTime());
        Assert.assertEquals(expected, toValue);
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
