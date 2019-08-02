package com.nubeiot.core.sql.converter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateConverterTest {

    private DateConverter converter;
    private TimeZone zoneDef;

    @Before
    public void before() {
        zoneDef = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        this.converter = new DateConverter();
    }

    @After
    public void after() {
        TimeZone.setDefault(zoneDef);
    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test
    public void test_from_end_date() {
        long currentTime = Long.parseLong("1549063220000"); // milliseconds of 01/02/2019 23:20:20 UTC
        LocalDate convertedDate = this.converter.from(new Date(currentTime));
        LocalDate localDate = LocalDate.parse("2019-02-01", DateTimeFormatter.ISO_LOCAL_DATE);
        Assert.assertEquals(localDate, convertedDate);
    }

    @Test
    public void test_from_start_date() {
        long currentTime = Long.parseLong("1548980420000"); // milliseconds of 01/02/2019 0:20:20 UTC
        LocalDate convertedDate = this.converter.from(new Date(currentTime));
        LocalDate localDate = LocalDate.parse("2019-02-01", DateTimeFormatter.ISO_LOCAL_DATE);
        Assert.assertEquals(localDate, convertedDate);
    }

    @Test
    public void test_to_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_date() {
        long currentTime = Long.parseLong("1549015393580"); // milliseconds of 01/02/2019

        LocalDate localDate = LocalDate.parse("2019-02-01", DateTimeFormatter.ISO_LOCAL_DATE);
        Date convertedDate = this.converter.to(localDate);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Assert.assertEquals(simpleDateFormat.format(convertedDate), simpleDateFormat.format(new Date(currentTime)));

        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        Assert.assertEquals(simpleDateFormat1.format(convertedDate), simpleDateFormat1.format(new Date(currentTime)));
    }

    @Test
    public void test_from_class() {
        Assert.assertEquals(this.converter.fromType(), Date.class);
    }

    @Test
    public void test_to_class() {
        Assert.assertEquals(this.converter.toType(), LocalDate.class);
    }

}
