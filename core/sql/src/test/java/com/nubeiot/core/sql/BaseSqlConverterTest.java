package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;

import lombok.NonNull;

abstract class BaseSqlConverterTest extends BaseSqlTest {

    protected MockManyEntityHandler entityHandler;

    @Before
    public void before(TestContext context) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        super.before(context);
        setup(context, handler(), function());
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    protected abstract Class<? extends MockManyEntityHandler> handler();

    protected abstract Function<TblSample_01, Single<Integer>> function();

    private <T extends MockManyEntityHandler> void setup(TestContext context, Class<T> clazz,
                                                         Function<TblSample_01, Single<Integer>> function) {
        this.entityHandler = startSQL(context, ManySchema.CATALOG, clazz);
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
        Observable.fromArray(pojo1, pojo2).flatMapSingle(function::apply).subscribe(r -> {
            System.out.print("Insert successfully!");
            TestHelper.testComplete(async);
        });
    }

    protected <T> void queryAndAssert(TestContext context, Async async, int id, @NonNull String field,
                                      @NonNull T data) {
        this.entityHandler.getSample01Dao()
                          .findOneById(id)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), field, data));
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
