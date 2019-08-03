package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.function.Function;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;

abstract class BaseSqlDaoConverterTest extends BaseSqlConverterTest {

    protected abstract Function<TblSample_01, Single<Integer>> function();

    @Override
    protected void initData(TestContext context) {
        TblSample_01 pojo1 = new TblSample_01().setId(1)
                                               .setFDate_1(LocalDate.of(2019, 2, 17))
                                               .setFTimestamp(
                                                   OffsetDateTime.of(2019, 2, 17, 23, 59, 59, 0, ZoneOffset.UTC))
                                               .setFTimestampz(
                                                   OffsetDateTime.of(2019, 2, 17, 23, 59, 59, 0, ZoneOffset.UTC))
                                               .setFTime(LocalTime.of(23, 59, 59))
                                               .setFDuration(Duration.ofHours(50).plusMinutes(30).plusSeconds(20))
                                               .setFPeriod(Period.ofYears(2).plusMonths(3).plusDays(33));
        TblSample_01 pojo2 = new TblSample_01().setId(2)
                                               .setFDate_1(LocalDate.of(2019, 2, 17))
                                               .setFTimestamp(
                                                   OffsetDateTime.of(2019, 2, 17, 0, 0, 1, 0, ZoneOffset.UTC))
                                               .setFTimestampz(
                                                   OffsetDateTime.of(2019, 2, 17, 0, 0, 1, 0, ZoneOffset.UTC))
                                               .setFTime(LocalTime.of(0, 0, 1))
                                               .setFDuration(Duration.ofHours(50).plusMinutes(30).plusSeconds(20))
                                               .setFPeriod(Period.ofYears(2).plusMonths(3).plusDays(33));
        Async async = context.async(1);
        Observable.fromArray(pojo1, pojo2).flatMapSingle(function()::apply).subscribe(r -> {
            System.out.print("Insert successfully!");
            TestHelper.testComplete(async);
        });
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
