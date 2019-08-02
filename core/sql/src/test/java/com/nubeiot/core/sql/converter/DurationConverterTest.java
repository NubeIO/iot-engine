package com.nubeiot.core.sql.converter;

import java.time.Duration;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nubeiot.core.exceptions.DatabaseException;

public class DurationConverterTest {

    private DurationConverter converter;

    @Before
    public void before() {
        this.converter = new DurationConverter();
    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test(expected = DatabaseException.class)
    public void test_invalid_pattern() {
        Assert.assertNull(this.converter.from("P1M"));
    }

    @Test
    public void test_from_days_hours_and_minutes_pattern() {

        //2 days, 10 hours, 20 minutes
        Duration from = this.converter.from("P2DT10H20M");
        Assert.assertEquals(Objects.requireNonNull(from).toMillis(), (2 * 24 * 60 + 10 * 60 + 20) * 60 * 1000L);
    }

    @Test
    public void test_to_with_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_string() {
        Duration duration = Duration.ofDays(2).plusHours(2).plusMinutes(30).plusSeconds(20);
        Assert.assertEquals(this.converter.to(duration), "PT50H30M20S");
    }

    @Test
    public void test_from_class() {
        Assert.assertEquals(this.converter.fromType(), String.class);
    }

    @Test
    public void test_to_class() {
        Assert.assertEquals(this.converter.toType(), Duration.class);
    }

}
