package com.nubeiot.core.sql.converter;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimeConverterTest {

    private TimeConverter converter;

    @Before
    public void before() {
        this.converter = new TimeConverter();
    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test
    public void test_from_date() {
        LocalTime convertedDate = this.converter.from(Time.valueOf("00:20:20"));
        LocalTime localDate = LocalTime.parse("00:20:20", DateTimeFormatter.ISO_LOCAL_TIME);
        Assert.assertEquals(localDate, convertedDate);
    }

    @Test
    public void test_from_end_date() {
        LocalTime convertedDate = this.converter.from(Time.valueOf("23:20:20"));
        LocalTime localDate = LocalTime.parse("23:20:20", DateTimeFormatter.ISO_LOCAL_TIME);
        Assert.assertEquals(localDate, convertedDate);
    }

    @Test
    public void test_to_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_local_time() {
        LocalTime localTime = LocalTime.of(8, 18, 26);
        Time convertedTime = this.converter.to(localTime);
        Assert.assertEquals(Time.valueOf("08:18:26"), convertedTime);
    }

    @Test
    public void test_from_class() {
        Assert.assertEquals(this.converter.fromType(), Time.class);
    }

    @Test
    public void test_to_class() {
        Assert.assertEquals(this.converter.toType(), LocalTime.class);
    }

}
