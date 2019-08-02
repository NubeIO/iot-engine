package com.nubeiot.core.sql.converter;

import java.time.Period;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nubeiot.core.exceptions.DatabaseException;

public class PeriodConverterTest {

    private PeriodConverter converter;

    @Before
    public void before() {
        this.converter = new PeriodConverter();
    }

    @Test
    public void test_from_null() {
        Assert.assertNull(this.converter.from(null));
    }

    @Test(expected = DatabaseException.class)
    public void test_from_invalid_pattern() {
        this.converter.from("PT2Y");
    }

    @Test
    public void test_from_years_months_days() {
        Period period = Objects.requireNonNull(this.converter.from("P2Y3M4W5D"));
        Assert.assertEquals(period.getYears(), 2);
        Assert.assertEquals(period.getMonths(), 3);
        Assert.assertEquals(period.getDays(), 33);
    }

    @Test
    public void test_to_with_null() {
        Assert.assertNull(this.converter.to(null));
    }

    @Test
    public void test_to_string() {
        Period period = Period.ofYears(2).plusMonths(2).plusDays(30);
        Assert.assertEquals(this.converter.to(period), "P2Y2M30D");
    }

    @Test
    public void test_from_class() {
        Assert.assertEquals(this.converter.fromType(), String.class);
    }

    @Test
    public void test_to_class() {
        Assert.assertEquals(this.converter.toType(), Period.class);
    }

}
