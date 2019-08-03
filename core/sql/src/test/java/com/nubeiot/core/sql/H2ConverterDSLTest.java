package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class H2ConverterDSLTest extends BaseSqlDslConverterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void beforeSuite() { BaseSqlTest.beforeSuite(); }

    @Override
    protected Class<? extends MockManyEntityHandler> handler() { return MockManyNoData.class; }

    @Override
    @NonNull String getJdbcUrl() {
        return "jdbc:h2:file:" + folder.getRoot().toPath().resolve("dbh2local").toString();
    }

    @Test
    public void test_date_converter(TestContext context) {
        Async async = context.async(2);
        queryAndAssert(context, async, 1, "fDate_1", LocalDate.of(2019, 2, 17));
        queryAndAssert(context, async, 2, "fDate_1", LocalDate.of(2019, 2, 17));
    }

    @Test
    public void test_timestamp_converter(TestContext context) {
        Async async = context.async(2);
        queryAndAssert(context, async, 1, "fTimestamp", OffsetDateTime.of(2019, 2, 17, 23, 59, 59, 0, ZoneOffset.UTC));
        queryAndAssert(context, async, 2, "fTimestamp", OffsetDateTime.of(2019, 2, 17, 0, 0, 1, 0, ZoneOffset.UTC));
    }

    @Test
    public void test_time_converter(TestContext context) {
        Async async = context.async(2);
        queryAndAssert(context, async, 1, "fTime", LocalTime.of(23, 59, 59));
        queryAndAssert(context, async, 2, "fTime", LocalTime.of(0, 0, 1));
    }

    @Test
    public void test_duration_converter(TestContext context) {
        Async async = context.async(1);
        ////f_duration = PT50H30M20S
        queryAndAssert(context, async, 1, "fDuration", Duration.ofHours(50).plusMinutes(30).plusSeconds(20));
    }

    @Test
    public void test_period_converter(TestContext context) {
        Async async = context.async(1);
        //f_period= P2Y3M4W5D
        queryAndAssert(context, async, 1, "fPeriod", Period.ofYears(2).plusMonths(3).plusDays(33));
    }

}
