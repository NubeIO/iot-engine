package com.nubeiot.core.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nubeiot.core.exceptions.NubeException;

public class DateTimesTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_parseISO8601() {
        String input = "2019-01-27T7:00:00.000+07:00";
        String expected = "2019-01-27T00:00:00Z";
        Assert.assertEquals(DateTimes.parseISO8601(input).toString(), expected);
    }

    @Test
    public void test_parseISO8601_with_wrong_format_should_throw_NubeException() {
        thrown.expect(NubeException.class);
        thrown.expectMessage("Invalid date");

        DateTimes.parseISO8601("2019-01-27");
    }

    @Test
    public void test_get_local_date_time_in_UTC_from_time_zone_HaNoi() {
        Instant instant = LocalDateTime.of(2019, 1, 27, 7, 0, 0, 0).atZone(ZoneId.of("GMT+7")).toInstant();
        LocalDateTime localDateTime = DateTimes.fromUTC(instant);
        Assert.assertEquals(localDateTime.toString(), "2019-01-27T00:00");
    }

    @Test
    public void test_get_offset_date_time_in_UTC_from_time_zone_HaNoi() {
        Instant instant = LocalDateTime.of(2019, 1, 27, 7, 0, 0, 0).atZone(ZoneId.of("GMT+7")).toInstant();
        OffsetDateTime offsetDateTime = DateTimes.from(instant);
        Assert.assertEquals(offsetDateTime.toString(), "2019-01-27T00:00Z");
    }

}
